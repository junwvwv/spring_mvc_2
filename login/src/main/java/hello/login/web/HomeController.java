package hello.login.web;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

   // @GetMapping("/")
    public String home() {
        return "home";
    }

    /**
     * @CookieValue 를 사용하면 편리하게 쿠키를 조회할 수 있다
     * 로그인 하지 않은 사용자도 홈에 접근할 수 있으니 required = false
     */
    //@GetMapping("/")
    public String homeLoginV1(@CookieValue(name = "memberId", required = false) Long memberId, Model model) {
        if (memberId == null) {
            return "home";
        }
        //로그인
        Member loginMember = memberRepository.findById(memberId);
        if (loginMember == null) {
            return "home";
        }
        model.addAttribute("member", loginMember);
        return "loginHome";
    }

    //@GetMapping("/")
    public String homeLoginV2(HttpServletRequest request, Model model) {

        //세션 관리자에 저장된 회원 정보 조회
        Member member = (Member) sessionManager.getSession(request);

        if (member == null) {
            return "home";
        }

        model.addAttribute("member", member);
        return "loginHome";
    }

    //@GetMapping("/")
    public String homeLoginV3(HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);
        if(session == null){
            return "home";
        }

        //세션에 저정된 회원 정보 조회
        Member member = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (member == null) {
            return "home";
        }

        model.addAttribute("member", member);
        return "loginHome";
    }

    @GetMapping("/")
    public String homeLoginV3Spring(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member member,
                                    Model model) {

        //@SessionAttribute 로 편리하게 조회 - 이 기능은 세션을 생성하지 않는다
        if (member == null) {
            return "home";
        }

        model.addAttribute("member", member);
        return "loginHome";
    }

}