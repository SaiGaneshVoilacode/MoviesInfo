package com.packetprep;

import com.packetprep.*;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
@WebServlet("/Movies")
public class Movies extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String movieTitle = req.getParameter("movieTitle");
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		
		if (movieTitle != null && !movieTitle.isEmpty()) 
		{
            // API endpoint URL for searching movies by title
            String apiUrl = "https://api.themoviedb.org/3/search/movie?api_key=56fab4576578de6ddfd456656b0b98d9&query=" + movieTitle;

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) 
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String responseData = reader.lines().collect(Collectors.joining());
                connection.disconnect();

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> apiResponse = objectMapper.readValue(responseData, Map.class);

                // Access and display movie data in your JSP
                List<Map<String, Object>> results = (List<Map<String, Object>>) apiResponse.get("results");

                if (!results.isEmpty())
                {
                	try {
                		Class.forName("com.mysql.jdbc.Driver");
                		Connection connect = DriverManager.getConnection("jdbc:mysql://localhost:3307/packetprep?useSSL=false", "root", "12345");
                		PreparedStatement ps = connect.prepareStatement("insert into moviesinfo(MovieName, ReleaseDate, overview) values(?, ?, ?)");
	                	out.println("<html><head><link rel='stylesheet' href='https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css'></head>");
	                	out.println("<body class='bg-gray-200'><div class='container mt-5'>");
	                	out.println("<table class='table table-bordered table-striped mx-auto shadow-lg mt-10 rounded-lg'>");
	                	out.println("<thead class='thead-dark'><tr><th>Movie Title</th><th>Release Date</th><th>Overview</th></tr></thead><tbody>");
	                	for(Map<String, Object> movie : results) {
	                		ps.setString(1, (String) movie.get("title"));
	                		ps.setString(2, (String) movie.get("release_date"));
	                		ps.setString(3, (String) movie.get("overview"));
	                		int num =ps.executeUpdate();
	                		if(num!=0) {
	                			out.println("<tr><td>" + movie.get("title") + "</td><td>" + movie.get("release_date") + "</td><td>" + movie.get("overview") + "</td></tr>");
	                		}
	                		else {
	                			out.println("Data not inserted..");
	                		}
	         
	                	}
	
	                	out.println("</tbody></table></div></body></html>");
                	}
                	catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

                } 
                else
                {
                    out.println("<p>No results found for the movie title: " + movieTitle + "</p>");
                }
            } 
            else 
            {
                out.println("Error: " + responseCode);
            }
        } 
		else 
		{
            out.println("<p>Please enter a movie title.</p>");
        }        
    }
}
