package com.nano.soft.kol.constants;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nano.soft.kol.bloger.entity.Bloger;
import com.nano.soft.kol.bloger.repo.BlogerRepository;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Component
@RequiredArgsConstructor
public class BlogerCache {

    private final BlogerRepository blogerRepository;
    private Integer minAge = Integer.MAX_VALUE;
    private Integer maxAge = 0;
    private Integer minSalary = Integer.MAX_VALUE;
    private Integer maxSalary = 0;

    @PostConstruct
    public void init() {
        List<Bloger> blogers = blogerRepository.findAll();
        for (Bloger bloger : blogers) {
            // Update Age
            if (bloger.getDateOfBirth() != null) {
                LocalDate birthDate = bloger.getDateOfBirth()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                LocalDate currentDate = LocalDate.now();
                int age = Period.between(birthDate, currentDate).getYears();

                // Update min and max age
                if (age < minAge) {
                    minAge = age;
                }
                if (age > maxAge) {
                    maxAge = age;
                }
            }

            // Update Salary
            if (bloger.getPrice() != null) {
                Integer price = bloger.getPrice();
                if (price < minSalary) {
                    minSalary = price;
                }
                if (price > maxSalary) {
                    maxSalary = price;
                }
            }
        }

        // Handle cases where no Blogers are present
        if (minAge == Integer.MAX_VALUE) {
            minAge = 0; // or any default value you prefer
        }
        if (minSalary == Integer.MAX_VALUE) {
            minSalary = 0; // or any default value you prefer
        }
    }

    public void refresh() {
        init();
    }
}
