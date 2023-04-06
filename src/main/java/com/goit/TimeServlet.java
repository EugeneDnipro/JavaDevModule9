package com.goit;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;


@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;
    String timezone;
    HttpServletRequest req;
    HttpServletResponse resp;

    @Override
    public void init() {
        engine = new TemplateEngine();
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setSuffix(".html");
        //resolver.setPrefix("templates/");
        resolver.setTemplateMode("HTML5");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=utf-8");
        timezone = req.getParameter("timezone");

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("timeString", getCurrentUtcTime(checkingLastTimezoneCookie(timezone, req, resp)));

        Context timeContext = new Context(
                req.getLocale(),
                Map.of("queryParams", params)
        );

        engine.process("timePage", timeContext, resp.getWriter());
        resp.getWriter().close();
    }

    public static String getCurrentUtcTime(String timezone) {
//        if (timezone == null) {
//            timezone = "UTC";
//        } else if (timezone.contains(" ")) {
//            timezone = timezone.replace(" ", "+");
//        }
        ZoneId zone = ZoneId.of(timezone);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zone);
        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss '" + timezone + "'"));
    }

    public static String checkingLastTimezoneCookie(String tz, HttpServletRequest req, HttpServletResponse resp) {
        if (tz != null && tz.contains(" ")) {
            tz = tz.replace(" ", "+");
        }

        Cookie[] cookies = req.getCookies();
        if (cookies.length == 0) {
            if (tz == null) {
                tz = "UTC";
            } else {
                resp.addCookie(new Cookie("lastTimezone", tz));
            }
        } else if (tz == null) {
            tz = cookies[0].getValue();
        }

        return tz;
    }
}
