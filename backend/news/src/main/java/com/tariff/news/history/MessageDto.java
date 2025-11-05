package com.tariff.news.history;

public class MessageDto {
    private String query;
    private String response;
    private Object sources;

    public MessageDto() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public Object getSources() { return sources; }
    public void setSources(Object sources) { this.sources = sources; }
}