package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.ChatRoom;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.ChatMessageDto;
import com.ssafynity.demo.service.ChatMessageService;
import com.ssafynity.demo.service.ChatRoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 채팅방 REST + MVC 컨트롤러
 *
 * GET  /chat          → 채팅방 목록
 * GET  /chat/{id}     → 채팅방 입장 (UI)
 * POST /chat          → 채팅방 생성
 * DELETE /chat/{id}   → 채팅방 삭제 (방장/관리자)
 * GET  /chat/{id}/history → 최근 메시지 JSON (REST API)
 */
@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    /** 채팅방 목록 */
    @GetMapping
    public String list(Model model, HttpSession session) {
        List<ChatRoom> rooms = chatRoomService.findAll();
        model.addAttribute("rooms", rooms);
        model.addAttribute("loginMember", session.getAttribute("loginMember"));
        return "chat/rooms";
    }

    /** 채팅방 입장 (채팅 UI) */
    @GetMapping("/{id}")
    public String enter(@PathVariable Long id, Model model, HttpSession session) {
        ChatRoom room = chatRoomService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        Member loginMember = (Member) session.getAttribute("loginMember");

        // 이전 메시지 로드 (Redis 캐시 → DB fallback)
        List<ChatMessageDto> history = chatMessageService.getRecentMessages(id);

        model.addAttribute("room", room);
        model.addAttribute("history", history);
        model.addAttribute("loginMember", loginMember);
        return "chat/room";
    }

    /** 채팅방 생성 (POST /chat) */
    @PostMapping
    public String create(@RequestParam String name,
                         @RequestParam(required = false) String description,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/member/login";
        }
        ChatRoom room = chatRoomService.create(name, description, loginMember);
        return "redirect:/chat/" + room.getId();
    }

    /** 채팅방 삭제 (방장 또는 관리자) */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        ChatRoom room = chatRoomService.findById(id).orElse(null);
        if (room == null) {
            redirectAttributes.addFlashAttribute("error", "채팅방을 찾을 수 없습니다.");
            return "redirect:/chat";
        }
        boolean isCreator = room.getCreator() != null && room.getCreator().getId().equals(loginMember.getId());
        boolean isAdmin = "ADMIN".equals(loginMember.getRole());
        if (!isCreator && !isAdmin) {
            redirectAttributes.addFlashAttribute("error", "권한이 없습니다.");
            return "redirect:/chat";
        }
        chatRoomService.delete(id);
        redirectAttributes.addFlashAttribute("success", "채팅방이 삭제되었습니다.");
        return "redirect:/chat";
    }

    /**
     * 최근 메시지 이력 JSON API (REST)
     * 페이지 첫 진입 시 JS fetch 로 호출하거나 서버사이드 렌더링 대체 가능.
     */
    @GetMapping("/{id}/history")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDto>> history(@PathVariable Long id) {
        List<ChatMessageDto> messages = chatMessageService.getRecentMessages(id);
        return ResponseEntity.ok(messages);
    }
}
