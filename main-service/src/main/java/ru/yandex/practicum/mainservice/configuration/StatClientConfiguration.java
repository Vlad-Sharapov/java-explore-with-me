package ru.yandex.practicum.mainservice.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.statclient.StatClient;

@Configuration
public class StatClientConfiguration {


    @Bean
    public StatClient statClientInit() {
        return new StatClient(new RestTemplate());
    }

    @Bean
    public ClientHandler clientHandlerInit() {
        return new ClientHandler(statClientInit());
    }
}
