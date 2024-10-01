package com.nano.soft.kol.bloger.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    private Integer balance;
    private ArrayList<CampaignReq> campaigns = new ArrayList<>();
}
