package com.nano.soft.kol.bloger.dto;

import java.util.ArrayList;

import com.nano.soft.kol.bloger.entity.Bloger;
import com.nano.soft.kol.bloger.entity.Category;
import com.nano.soft.kol.bloger.entity.CategoryNumber;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDTO {

    ArrayList<CategoryNumber> categories;
    ArrayList<Bloger> blogers;
}
