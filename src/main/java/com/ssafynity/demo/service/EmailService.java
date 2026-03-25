package com.ssafynity.demo.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    /** 메일 서버가 설정되지 않은 경우 null이 됩니다 */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@ssafynity.com}")
    private String fromAddress;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    // ── 멘토에게: 멘토링 신청 알림 ─────────────────────────────────────────
    public void sendApplicationEmail(String mentorEmail, String mentorNickname,
                                      String menteeNickname, String message) {
        if (isNotConfigured()) {
            log.info("[EMAIL-SKIP] 메일 서버 미설정. 멘토링 신청 알림 → {}", mentorEmail);
            return;
        }
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(mentorEmail);
            helper.setSubject("[SSAFYnity] " + menteeNickname + "님이 멘토링을 신청했습니다");
            helper.setText(buildApplicationHtml(mentorNickname, menteeNickname, message), true);
            mailSender.send(mail);
            log.info("[EMAIL] 멘토링 신청 이메일 발송 완료 → {}", mentorEmail);
        } catch (Exception e) {
            log.warn("[EMAIL-FAIL] 이메일 발송 실패 → {} : {}", mentorEmail, e.getMessage());
        }
    }

    // ── 멘티에게: 멘토링 답변 결과 알림 ────────────────────────────────────
    public void sendReplyEmail(String menteeEmail, String menteeNickname,
                                String mentorNickname, boolean accepted, String reply) {
        if (isNotConfigured()) {
            log.info("[EMAIL-SKIP] 메일 서버 미설정. 멘토링 답변 알림 → {}", menteeEmail);
            return;
        }
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(menteeEmail);
            String subject = accepted
                    ? "[SSAFYnity] 🎉 " + mentorNickname + " 멘토님이 멘토링을 수락했습니다!"
                    : "[SSAFYnity] " + mentorNickname + " 멘토님의 멘토링 신청 결과 안내";
            helper.setSubject(subject);
            helper.setText(buildReplyHtml(menteeNickname, mentorNickname, accepted, reply), true);
            mailSender.send(mail);
            log.info("[EMAIL] 멘토링 답변 이메일 발송 완료 → {}", menteeEmail);
        } catch (Exception e) {
            log.warn("[EMAIL-FAIL] 이메일 발송 실패 → {} : {}", menteeEmail, e.getMessage());
        }
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private boolean isNotConfigured() {
        return mailSender == null
                || "your-email@gmail.com".equals(fromAddress)
                || fromAddress == null || fromAddress.isBlank();
    }

    private String buildApplicationHtml(String mentorNickname, String menteeNickname, String message) {
        String safeMessage = escapeHtml(message);
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f5f5f7;margin:0;padding:40px 0;">
                  <div style="max-width:600px;margin:0 auto;padding:0 20px;">
                    <div style="background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 4px 30px rgba(0,0,0,.08);">
                      <!-- Header -->
                      <div style="background:linear-gradient(135deg,#0071e3,#0077ed);padding:36px 32px;text-align:center;">
                        <div style="font-size:1.6rem;font-weight:800;color:#fff;letter-spacing:-0.5px;">SSAFY<span style="opacity:.65;">nity</span></div>
                        <div style="font-size:.82rem;color:rgba(255,255,255,.7);margin-top:4px;letter-spacing:.06em;">멘토링 플랫폼</div>
                      </div>
                      <!-- Body -->
                      <div style="padding:36px 32px;">
                        <h2 style="margin:0 0 12px;font-size:1.25rem;color:#1d1d1f;font-weight:700;">안녕하세요, %s 멘토님 👋</h2>
                        <p style="color:#424245;line-height:1.75;margin:0 0 28px;font-size:.95rem;">
                          <strong style="color:#0071e3;">%s</strong>님이 멘토링을 신청했습니다.<br>
                          지금 SSAFYnity에서 신청 내용을 확인하고 답변해 주세요.
                        </p>
                        <!-- Message box -->
                        <div style="background:#f0f6ff;border-radius:14px;padding:22px 26px;margin-bottom:28px;border:1px solid rgba(0,113,227,.12);">
                          <div style="font-size:.72rem;font-weight:700;color:#0071e3;letter-spacing:.08em;text-transform:uppercase;margin-bottom:12px;">💬 신청 메시지</div>
                          <p style="color:#1d1d1f;line-height:1.8;margin:0;font-size:.93rem;white-space:pre-wrap;">%s</p>
                        </div>
                        <!-- CTA -->
                        <a href="%s/mentoring/my"
                           style="display:block;background:#0071e3;color:#ffffff;text-decoration:none;text-align:center;padding:16px 28px;border-radius:980px;font-weight:600;font-size:.95rem;letter-spacing:-.01em;">
                          ✅ 멘토링 신청 확인하기
                        </a>
                      </div>
                      <!-- Footer -->
                      <div style="padding:20px 32px;background:#f5f5f7;text-align:center;border-top:1px solid #e8e8ed;">
                        <p style="margin:0;font-size:.75rem;color:#86868b;">
                          이 이메일은 SSAFYnity에서 자동 발송되었습니다.<br>
                          수신을 원하지 않으시면 사이트 설정에서 이메일 알림을 비활성화하세요.
                        </p>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(mentorNickname, menteeNickname, safeMessage, baseUrl);
    }

    private String buildReplyHtml(String menteeNickname, String mentorNickname,
                                   boolean accepted, String reply) {
        String headerColor = accepted ? "linear-gradient(135deg,#34c759,#30b853)" : "linear-gradient(135deg,#636366,#4a4a4e)";
        String accentColor = accepted ? "#34c759" : "#636366";
        String emoji = accepted ? "🎉" : "📬";
        String statusText = accepted ? "수락" : "거절";
        String bodyText = accepted
                ? String.format("축하합니다! <strong style=\"color:#34c759;\">%s</strong> 멘토님이 멘토링 신청을 수락했습니다.<br>지금 바로 SSAFYnity에서 채팅을 통해 멘토링을 시작해보세요!", mentorNickname)
                : String.format("<strong>%s</strong> 멘토님이 이번에는 함께하기 어렵다고 답변하셨습니다.<br>다른 멘토에게도 도전하여 성장의 기회를 찾아보세요!", mentorNickname);

        String replyBlock = (reply != null && !reply.isBlank())
                ? """
                  <div style="background:#f5f5f7;border-radius:14px;padding:20px 24px;margin:20px 0;border-left:4px solid %s;">
                    <div style="font-size:.72rem;font-weight:700;color:#86868b;letter-spacing:.08em;text-transform:uppercase;margin-bottom:10px;">💌 멘토님의 답변</div>
                    <p style="color:#1d1d1f;line-height:1.8;margin:0;font-size:.93rem;white-space:pre-wrap;">%s</p>
                  </div>
                  """.formatted(accentColor, escapeHtml(reply))
                : "";

        String ctaText = accepted ? "💬 채팅 시작하기" : "🔍 다른 멘토 찾기";
        String ctaLink = accepted ? baseUrl + "/mentoring/my" : baseUrl + "/mentors";

        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f5f5f7;margin:0;padding:40px 0;">
                  <div style="max-width:600px;margin:0 auto;padding:0 20px;">
                    <div style="background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 4px 30px rgba(0,0,0,.08);">
                      <!-- Header -->
                      <div style="background:%s;padding:36px 32px;text-align:center;">
                        <div style="font-size:1.6rem;font-weight:800;color:#fff;letter-spacing:-0.5px;">SSAFY<span style="opacity:.65;">nity</span></div>
                        <div style="font-size:.82rem;color:rgba(255,255,255,.7);margin-top:4px;letter-spacing:.06em;">멘토링 플랫폼</div>
                      </div>
                      <!-- Body -->
                      <div style="padding:36px 32px;">
                        <h2 style="margin:0 0 12px;font-size:1.25rem;color:#1d1d1f;font-weight:700;">%s %s님, 멘토링 신청 결과입니다</h2>
                        <p style="color:#424245;line-height:1.75;margin:0 0 8px;font-size:.95rem;">%s</p>
                        %s
                        <!-- CTA -->
                        <a href="%s"
                           style="display:block;background:%s;color:#ffffff;text-decoration:none;text-align:center;padding:16px 28px;border-radius:980px;font-weight:600;font-size:.95rem;margin-top:24px;">
                          %s
                        </a>
                      </div>
                      <!-- Footer -->
                      <div style="padding:20px 32px;background:#f5f5f7;text-align:center;border-top:1px solid #e8e8ed;">
                        <p style="margin:0;font-size:.75rem;color:#86868b;">
                          이 이메일은 SSAFYnity에서 자동 발송되었습니다.
                        </p>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(headerColor, emoji, menteeNickname, bodyText, replyBlock, ctaLink, accentColor, ctaText);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
