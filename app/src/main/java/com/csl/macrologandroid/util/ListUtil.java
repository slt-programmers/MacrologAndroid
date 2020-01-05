package com.csl.macrologandroid.util;

import com.csl.macrologandroid.dtos.FoodResponse;
import com.csl.macrologandroid.dtos.IngredientResponse;
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

    public static boolean isFoodInIngredientList(String foodName, List<IngredientResponse> ingredients) {
        for (IngredientResponse ingredient : ingredients) {
            if (foodName.equals(ingredient.getFood().getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFoodInList(String foodName, List<FoodResponse> allFood) {
        for (FoodResponse food : allFood) {
            if (foodName.equals(food.getName())) {
                return true;
            }
        }
        return false;
    }

    public static PortionResponse getPortionFromListByName(String portionName, List<PortionResponse> allPortions) {
        for (PortionResponse portion : allPortions) {
            if (portionName.contains("gr)")) {
                portionName = portionName.substring(0, portionName.indexOf(" ("));
            }
            if (portion.getDescription().equals(portionName)) {
                return portion;
            }
        }
        return null;
    }

    public static PortionResponse getPortionFromListByName(String portionName, FoodResponse food) {
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

    public static PortionResponse getPortionFromListById(Long portionId, List<PortionResponse> portions) {
        for (PortionResponse portion : portions) {
            if (portionId.equals(portion.getId())) {
                return portion;
            }
        }
        return null;
    }


    public static PortionResponse getPortionFromListById(Long portionId, FoodResponse food) {
        List<PortionResponse> portions = food.getPortions();
        for (PortionResponse portion : portions) {
            if (portionId.equals(portion.getId())) {
                return portion;
            }
        }
        return null;
    }
}
