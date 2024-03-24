package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeServlet.class);
    private DateTimeFormatter formatter;
    private TemplateEngine engine;
    @Override
    public void init() throws ServletException{
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        engine = new TemplateEngine();

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix("src/main/webapp/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);

        LOGGER.info("Initialized time servlet");
    }
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        String timezone = req.getParameter("timezone");
        String lastTimezone = getLastTimezone(req);

        ZoneId zoneId = (timezone != null && !timezone.isEmpty()) ? getZoneId(timezone) :
                (lastTimezone != null ? ZoneId.of(lastTimezone) : ZoneId.systemDefault());

        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        String time = zonedDateTime.format(formatter);

        Context simpleContext = new Context(
                req.getLocale(),
                Map.of("queryParam", time)
        );
        String  encodedZoneId = URLEncoder.encode(String.valueOf(zoneId), StandardCharsets.UTF_8);
        if (lastTimezone == null && timezone!=null) {
            Cookie lastTimezoneCookie = new Cookie("lastTimezone", encodedZoneId);
            resp.addCookie(lastTimezoneCookie);
            LOGGER.info("Added cookie to user: "+req.getRemoteUser()+"; Cookie: "+lastTimezoneCookie);
        }

        engine.process("time", simpleContext, resp.getWriter());
        resp.getWriter().close();

        LOGGER.info("Request completed successful");
    }
    private ZoneId getZoneId(String timezone) {
        if (timezone == null || timezone.trim().isEmpty()) {
            return ZoneId.systemDefault();
        }
        LOGGER.info("Getting Zone Id");
        return ZoneId.of(timezone.replace(" ", "+"));
    }
    private String getLastTimezone(HttpServletRequest req){
        String lastTimezone = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("lastTimezone".equals(cookie.getName())) {
                    lastTimezone = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                    break;
                }
            }
        }

        LOGGER.info("Getting last time zone");
        return lastTimezone;
    }

    @Override
    public void destroy(){
        formatter = null;
        engine = null;
        LOGGER.info("Servlet destroyed");
    }
}
