package com.tariff.calculation.tariffCalc.utility;

// this class is just for quick testing of LemmaUtils methods
// SHOULD BE REMOVED LATER AFTER TESTING
// run with: ./mvnw exec:java -Dexec.mainClass="com.tariff.calculation.tariffCalc.utility.LemmaUtilsTest"

public class LemmaUtilsTest {

    public static void main(String[] args) {
        System.out.println("=== Testing LemmaUtils ===");

        // Test regular plurals
        testConversion("shoes", "shoe");
        testConversion("books", "book");
        testConversion("computers", "computer");
        testConversion("phones", "phone");

        // Test irregular plurals
        testConversion("children", "child");
        testConversion("men", "man");
        testConversion("women", "woman");
        testConversion("feet", "foot");
        testConversion("mice", "mouse");
        testConversion("slippers", "slipper");
        testConversion("mushrooms", "mushroom");

        // Test words ending in -ies
        testConversion("batteries", "battery");
        testConversion("companies", "company");
        testConversion("countries", "country");

        // Test words ending in -es
        testConversion("boxes", "box");
        testConversion("glasses", "glass");
        testConversion("watches", "watch");
        testConversion("ashes", "ash");

        // Test compound nouns
        testConversion("tennis shoes", "tennis shoe");
        testConversion("coffee tables", "coffee table");
        testConversion("cell phones", "cell phone");

        // Test singular words (should remain unchanged)
        testConversion("shoe", "shoe");
        testConversion("laptop", "laptop");
        testConversion("table", "table");

        // some extra tests
        testConversion("laptops", "laptop");
        testConversion("headphones", "headphone");
        testConversion("televisions", "television");
        testConversion("monitors", "monitor");
        testConversion("keyboards", "keyboard");
        testConversion("mice", "mouse");
        testConversion("printers", "printer");
        testConversion("scanners", "scanner");
        testConversion("cameras", "camera");
        testConversion("speakers", "speaker");
        testConversion("furniture", "furniture");
        testConversion("shoes", "shoe");
        testConversion("socks", "sock");
        testConversion("bags", "bag");
        testConversion("watches", "watch");
        testConversion("glasses", "glass");
        testConversion("bicycles", "bicycle");
        testConversion("motorcycles", "motorcycle");
        testConversion("vehicles", "vehicle");
        testConversion("toys", "toy");
        testConversion("books", "book");
        testConversion("tables", "table");
        testConversion("chairs", "chair");
        testConversion("colours", "colour");
        testConversion("colors", "color");

        System.out.println("\n=== All tests completed! ===");
    }

    private static void testConversion(String input, String expected) {
        String result = LemmaUtils.toSingular(input);
        boolean passed = expected.equals(result);
        System.out.println(String.format("toSingular('%s') = '%s' | Expected: '%s' | %s",
                input, result, expected, passed ? "✓ PASS" : "✗ FAIL"));
    }

    // Add this method to any existing class to test quickly
    public void testItemNameUtils() {
        System.out.println("Testing: " + LemmaUtils.toSingular("shoes")); // should print "shoe"
        System.out.println("Testing: " + LemmaUtils.toSingular("children")); // should print "child"
    }
}
