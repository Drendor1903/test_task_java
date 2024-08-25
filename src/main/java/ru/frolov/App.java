package ru.frolov;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.frolov.models.Ticket;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class App 
{
    public static void main( String[] args )
    {
        try {
            List<Ticket> tickets = getTickets();
            differenceBetweenAverageAndMedianPrice(tickets);
            minFlightTimeBetweenCities(tickets);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Ticket> getTickets() throws Exception {
        InputStream inputStream = App.class.getClassLoader().getResourceAsStream("tickets.json");

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(inputStream);
        JsonNode ticketsNode = jsonNode.get("tickets");

        return objectMapper.readValue(ticketsNode.toString(), new TypeReference<ArrayList<Ticket>>() {});
    }

    public static void minFlightTimeBetweenCities(List<Ticket> tickets){
        if (tickets != null) {
            Map<String, Duration> minTimes = tickets.stream()
                    .filter(ticket -> ticket.getOrigin().equals("VVO") && ticket.getDestination().equals("TLV"))
                    .collect(Collectors.groupingBy(Ticket::getCarrier,
                            Collectors.collectingAndThen(
                                    Collectors.mapping(
                                            ticket -> calculateDuration(ticket.getDepartureTime(), ticket.getArrivalTime()),
                                            Collectors.minBy(Duration::compareTo)
                                    ),
                                    Optional::orElseThrow)
                    ));

            minTimes.forEach((carrier, duration) ->
                    System.out.println("Перевозчик: " + carrier + " время полета: " + formatDuration(duration)));
        }
    }

    public static Duration calculateDuration(String departureTime, String arrivalTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        LocalTime departure = LocalTime.parse(departureTime, formatter);
        LocalTime arrival = LocalTime.parse(arrivalTime, formatter);

        return Duration.between(departure, arrival);
    }

    private static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%d hours %d minutes", hours, minutes);
    }

    public static void differenceBetweenAverageAndMedianPrice(List<Ticket> tickets){
        double median, average = 0.0;

        if (tickets != null) {
            List<Ticket> sortTickets = tickets.stream()
                    .filter(ticket -> ticket.getOrigin().equals("VVO") && ticket.getDestination().equals("TLV"))
                    .sorted(Comparator.comparingInt(Ticket::getPrice))
                    .toList();

            if (sortTickets.size() % 2 == 1){
                median = sortTickets.get(sortTickets.size() / 2).getPrice();
            } else {
                int m1 = sortTickets.size() / 2;
                int m2 = sortTickets.size() / 2 - 1;

                median = (double) (sortTickets.get(m1).getPrice() + sortTickets.get(m2).getPrice()) /2;
            }

            for (Ticket ticket:sortTickets){
                average += ticket.getPrice();
            }
            average = average/sortTickets.size();

            System.out.println("Разница между средней ценой и медианой для полета между городами Владивосток и Тель-Авив: " + (average - median));
        }

    }
}
