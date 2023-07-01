package com.etransportation.controller;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faker")
public class FakerController {

    @GetMapping("/fakerRandom")
    public ResponseEntity<?> fakerRandom() {
        Faker faker = new Faker();

        String streetName = faker.address().streetName();
        String number = faker.address().buildingNumber();
        String city = faker.address().city();
        String country = faker.address().country();
        String catBreed = faker.cat().breed();
        String catRegistry = faker.cat().registry();

        Map<String, String> map = new HashMap<>();
        map.put("streetName", streetName);
        map.put("number", number);
        map.put("city", city);
        map.put("country", country);
        map.put("catBreed", catBreed);
        map.put("catRegistry", catRegistry);

        return ResponseEntity.ok(map);
    }

    @GetMapping("/fakerRegex")
    public ResponseEntity<?> fakerRegex() {
        FakeValuesService fakeValuesService = new FakeValuesService(new Locale("en-GB"), new RandomService());

        String alphaNumericString = fakeValuesService.regexify("[a-z1-9]{10}");

        return ResponseEntity.ok(alphaNumericString);
    }
}
