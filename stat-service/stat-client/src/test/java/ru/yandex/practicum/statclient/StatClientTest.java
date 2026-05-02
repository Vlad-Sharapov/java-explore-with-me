package ru.yandex.practicum.statclient;

class StatClientTest {

//    StatClient statClient;
//
//    private final String statServerUrl = "http://localhost:9090";
//
//    @BeforeEach
//    public void beforeEach() {
//        statClient = new StatClient(new RestTemplate());
//    }
//
//    @Test
//    void get() {
//        HitDto firstHit = HitDto.builder()
//                .uri("/events/1")
//                .app("ewm-main-service")
//                .ip("121.0.0.1")
//                .timestamp(LocalDateTime.now().minusMinutes(5)
//                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
//                .build();
//        statClient.addHit(statServerUrl, firstHit);
//
//        HitDto secondHit = HitDto.builder()
//                .uri("/events/2")
//                .app("ewm-main-service")
//                .ip("121.0.0.2")
//                .timestamp(LocalDateTime.now().minusMinutes(3)
//                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
//                .build();
//        statClient.addHit(statServerUrl, secondHit);
//
//        ResponseEntity<List<StatsDto>> stat = statClient.getStat(statServerUrl, StatParamDto.builder()
//                .start(LocalDateTime.now().minusHours(8))
//                .end(LocalDateTime.now().plusHours(1))
//                .uris(List.of("/events/1", "/events/2"))
//                .build());
//        List<StatsDto> statsDtos1 = stat.getBody();
//        assertThat(statsDtos1, hasSize(2));
//        assertThat(statsDtos1, everyItem(hasProperty("hits", equalTo(1L))));
//
//    }
//
//    @Test
//    void post() {
//        HitDto hitDto = HitDto.builder()
//                .uri("/events")
//                .app("ewm-main-service")
//                .ip("121.0.0.1")
//                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).build();
//        ResponseEntity<HitDto> hitDtoResponseEntity = statClient.addHit(statServerUrl, hitDto);
//        HitDto saveHitDto = hitDtoResponseEntity.getBody();
//
//        assertThat(saveHitDto, allOf(
//                hasProperty("id", notNullValue()),
//                hasProperty("uri", equalTo(hitDto.getUri())),
//                hasProperty("app", equalTo(hitDto.getApp())),
//                hasProperty("ip", equalTo(hitDto.getIp())),
//                hasProperty("timestamp", equalTo(hitDto.getTimestamp()))
//        ));
//    }
}
