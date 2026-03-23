package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.DirectMessage;
import com.ssafynity.demo.domain.DirectRoom;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.service.DirectMessageService;
import com.ssafynity.demo.service.FriendshipService;
import com.ssafynity.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DM / 그룹 채팅 HTTP 컨트롤러
 *
 * GET  /dm                        → 내 대화 목록
 * POST /dm/start/{friendId}       → DM 시작 (없으면 생성)
 * POST /dm/group                  → 그룹 채팅 생성
 * GET  /dm/{roomId}               → 채팅방 입장
 * POST /dm/{roomId}/invite/{id}   → 멤버 초대 (그룹)
 * POST /dm/{roomId}/leave         → 나가기
 */
@Controller
@RequestMapping("/dm")
@RequiredArgsConstructor
public class DmController {

    private final DirectMessageService directMessageService;
    private final MemberService memberService;
    private final FriendshipService friendshipService;

    /** 내 대화 목록 */
    @GetMapping
    public String list(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        Member fresh = memberService.findById(loginMember.getId()).orElseThrow();

        List<DirectRoom> rooms = directMessageService.getRoomsForMember(fresh);

        // 각 방 정보 보강
        Map<Long, Member> dmOtherMember = new HashMap<>();
        Map<Long, List<Member>> roomMembers = new HashMap<>();
        Map<Long, DirectMessage> lastMessages = new HashMap<>();

        for (DirectRoom room : rooms) {
            List<Member> members = directMessageService.getMemberList(room);
            roomMembers.put(room.getId(), members);
            if ("DM".equals(room.getType())) {
                members.stream()
                        .filter(m -> !m.getId().equals(fresh.getId()))
                        .findFirst()
                        .ifPresent(m -> dmOtherMember.put(room.getId(), m));
            }
            directMessageService.getLastMessage(room)
                    .ifPresent(msg -> lastMessages.put(room.getId(), msg));
        }

        model.addAttribute("loginMember", fresh);
        model.addAttribute("rooms", rooms);
        model.addAttribute("dmOtherMember", dmOtherMember);
        model.addAttribute("roomMembers", roomMembers);
        model.addAttribute("lastMessages", lastMessages);
        model.addAttribute("myFriends", friendshipService.getFriends(fresh));
        return "dm/list";
    }

    /** 친구와 1:1 DM 시작 (없으면 생성, 있으면 기존 방 이동) */
    @PostMapping("/start/{friendId}")
    public String startDm(@PathVariable Long friendId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        Member fresh = memberService.findById(loginMember.getId()).orElseThrow();
        Member friend = memberService.findById(friendId).orElseThrow();
        DirectRoom room = directMessageService.findOrCreateDm(fresh, friend);
        return "redirect:/dm/" + room.getId();
    }

    /** 그룹 채팅 생성 */
    @PostMapping("/group")
    public String createGroup(@RequestParam String name,
                              @RequestParam(required = false) List<Long> memberIds,
                              HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        Member fresh = memberService.findById(loginMember.getId()).orElseThrow();
        List<Long> ids = memberIds != null ? memberIds : new ArrayList<>();
        DirectRoom room = directMessageService.createGroup(fresh, name, ids);
        return "redirect:/dm/" + room.getId();
    }

    /** 채팅방 입장 */
    @GetMapping("/{roomId}")
    public String enterRoom(@PathVariable Long roomId, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        Member fresh = memberService.findById(loginMember.getId()).orElseThrow();

        DirectRoom room = directMessageService.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 참여자 아닌 경우 목록으로
        if (!directMessageService.isMember(room, fresh)) return "redirect:/dm";

        List<DirectMessage> messages = directMessageService.getMessages(room);
        List<Member> members = directMessageService.getMemberList(room);
        Set<Long> memberIdSet = members.stream().map(Member::getId).collect(Collectors.toSet());

        // 방 제목 + DM 상대방
        Member otherMember = null;
        String roomTitle;
        if ("DM".equals(room.getType())) {
            otherMember = members.stream()
                    .filter(m -> !m.getId().equals(fresh.getId()))
                    .findFirst().orElse(null);
            roomTitle = otherMember != null ? otherMember.getNickname() : "DM";
        } else {
            roomTitle = room.getName();
        }

        // 초대 가능한 친구 (이미 방에 없는 친구만)
        List<Member> invitableFriends = friendshipService.getFriends(fresh).stream()
                .filter(f -> !memberIdSet.contains(f.getId()))
                .collect(Collectors.toList());

        model.addAttribute("loginMember", fresh);
        model.addAttribute("room", room);
        model.addAttribute("messages", messages);
        model.addAttribute("members", members);
        model.addAttribute("roomTitle", roomTitle);
        model.addAttribute("otherMember", otherMember);
        model.addAttribute("invitableFriends", invitableFriends);
        return "dm/room";
    }

    /** 그룹 채팅에 멤버 초대 */
    @PostMapping("/{roomId}/invite/{memberId}")
    public String invite(@PathVariable Long roomId, @PathVariable Long memberId,
                         HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        DirectRoom room = directMessageService.findById(roomId).orElseThrow();
        Member invitee = memberService.findById(memberId).orElseThrow();
        directMessageService.addMember(room, invitee);
        return "redirect:/dm/" + roomId;
    }

    /** 채팅방 나가기 */
    @PostMapping("/{roomId}/leave")
    public String leave(@PathVariable Long roomId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        Member fresh = memberService.findById(loginMember.getId()).orElseThrow();
        DirectRoom room = directMessageService.findById(roomId).orElseThrow();
        directMessageService.leaveRoom(room, fresh);
        return "redirect:/dm";
    }
}
