package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    String response;
    DateTimeFormatter formatter;
    @Override
    public void init() throws ServletException{
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    }
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String timezone = req.getParameter("timezone");
        if (timezone != null){
            if (!timezone.contains("-")){
                timezone = timezone.replace(" ", "+");
            }
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
            response = zonedDateTime.format(formatter);
        } else {
            ZonedDateTime defaultTime = ZonedDateTime.now();
            response = defaultTime.format(formatter);
        }
        resp.setContentType("text/html; charset=utf-8");
        resp.getWriter().write(response);
        resp.getWriter().close();
    }
    @Override
    public void destroy(){
        response = null;
        formatter = null;
    }
}
