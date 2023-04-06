package com.goit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.TimeZone;

@WebFilter("/time/*")
public class TimezoneValidateFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String timeZoneId;
        String timezone = req.getParameter("timezone");
        if (timezone == null) {
            timeZoneId = "UTC";
        } else if ((timezone.contains(" ")) || (timezone.contains("-"))) {
            String[] splitted = timezone.split("[\\s\\-]");
            timeZoneId = splitted[0];
        } else {
            timeZoneId = timezone;
        }
        if (Arrays.asList(TimeZone.getAvailableIDs()).contains(timeZoneId)) {
            chain.doFilter(req, resp);
        } else {
            resp.sendError(400, "Error: Invalid timezone");
            resp.getWriter().close();
        }
    }
}