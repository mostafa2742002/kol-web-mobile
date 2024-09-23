package com.nano.soft.kol.bloger.dto;

import java.util.ArrayList;

import com.nano.soft.kol.bloger.entity.Bloger;
import com.nano.soft.kol.bloger.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDTO {

    ArrayList<Category> categories;
    ArrayList<Bloger> blogers;
}
