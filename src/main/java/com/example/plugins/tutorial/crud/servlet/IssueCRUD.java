package com.example.plugins.tutorial.crud.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IssueCRUD extends HttpServlet{
    private static final Logger log = LoggerFactory.getLogger(IssueCRUD.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        resp.getWriter().write("<html><body>Hello World</body></html>");
    }

}