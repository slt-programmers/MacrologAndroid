package com.csl.macrologandroid.util;

import com.csl.macrologandroid.dtos.FoodResponse;
import com.csl.macrologandroid.dtos.PortionResponse;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {

    public static List<String> getPortionDescList(List<PortionResponse> portionList, boolean includeGrams) {
        List<String> list = new ArrayList<>();
        for (PortionResponse portion : portionList) {
            String desc = portion.getDescription();
            if (desc != null && !desc.isEmpty()) {
                if (includeGrams) {
                    list.add(desc + " (" + portion.getGrams() + " gr)");
                } else {
                    list.add(desc);
                }
            }
        }
        list.add("gram");
        return list;
    }

    public static PortionResponse getPortionFromFoodByName(String portionName, FoodResponse food) {
        List<PortionResponse> portions = food.getPortions();
        for (PortionResponse portion : portions) {
            if (portionName.contains("gr)")) {
                portionName = portionName.substring(0, portionName.indexOf(" ("));
            }

            if (portion.getDescription().equals(portionName)) {
                return portion;
            }
        }
        return null;
    }

    public static PortionResponse getPortionFromListById(List<PortionResponse> portions, Long portionId) {
        for (PortionResponse portion : portions) {
            if (portionId.equals(portion.getId())) {
                return portion;
            }
        }
        return null;
    }
 }
