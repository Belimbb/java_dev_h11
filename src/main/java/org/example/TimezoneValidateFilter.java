package org.example;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter (value = "/time")
public class TimezoneValidateFilter extends HttpFilter {
    String filterRegex;
    @Override
    public void init() throws ServletException {
        filterRegex = "UTC[ -](1[0-2]|[1-9])";
    }
    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String timezone = req.getParameter("timezone");
        if (timezone!=null){
            if (timezone.matches(filterRegex)){
                chain.doFilter(req, resp);
            } else {
                resp.setStatus(401);

                resp.setContentType("text/html");
                resp.getWriter().write("Invalid timezone");
                resp.getWriter().close();
            }
        }else {
            chain.doFilter(req, resp);
        }
    }
    @Override
    public void destroy() {
        filterRegex = null;
    }
}
