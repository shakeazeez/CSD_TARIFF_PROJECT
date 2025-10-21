package com.user.enums;

public enum Industry {
    AGRICULTURE("Agriculture", "Involves farming, crop cultivation, livestock breeding, aquaculture, forestry, horticulture, and related activities for food production, fiber, biofuels, and raw materials. Includes tractors, fertilizers, seeds, irrigation systems, dairy farming, poultry, fishing gear, timber harvesting, and agricultural machinery."),
    MANUFACTURING("Manufacturing", "Encompasses the production of goods through mechanical, chemical, or manual processes, including assembly lines, machinery operation, quality control, supply chain management, CNC machines, robotics, welding, molding, packaging equipment, and industrial automation systems."),
    RETAIL("Retail", "Focuses on selling consumer goods and services directly to customers through stores, online platforms, supermarkets, specialty shops, e-commerce, point-of-sale systems, inventory management, customer service, display fixtures, and retail software."),
    HEALTHCARE("Healthcare", "Provides medical, dental, nursing, pharmaceutical, and therapeutic services, including hospitals, clinics, diagnostic centers, health insurance, medical devices, surgical instruments, pharmaceuticals, MRI machines, hospital beds, and telemedicine platforms."),
    TECHNOLOGY("Technology", "Develops and maintains software, hardware, IT infrastructure, cybersecurity, data analytics, AI, cloud computing, digital solutions, servers, laptops, smartphones, networking equipment, software licenses, and IoT devices."),
    FINANCE("Finance", "Manages banking, investments, insurance, financial planning, risk assessment, lending, payments, regulatory compliance, ATMs, trading platforms, credit cards, financial software, blockchain, and cryptocurrency services."),
    EDUCATION("Education", "Delivers teaching, training, research, and learning services through schools, universities, online courses, vocational programs, educational materials, textbooks, e-learning platforms, laboratory equipment, and educational software."),
    CONSTRUCTION("Construction", "Builds infrastructure, residential and commercial buildings, roads, bridges, using engineering, project management, construction techniques, cranes, bulldozers, concrete mixers, scaffolding, surveying equipment, and building materials."),
    TRANSPORTATION("Transportation", "Handles logistics, shipping, freight, passenger travel, vehicle maintenance, traffic management, transportation safety, trucks, ships, airplanes, railways, GPS systems, fleet management, and transportation hubs."),
    ENERGY("Energy", "Produces, distributes, and manages power from sources like oil, gas, coal, nuclear, solar, wind, hydroelectric, energy efficiency, power plants, transformers, solar panels, wind turbines, batteries, and smart grids."),
    REAL_ESTATE("Real Estate", "Deals with property development, buying/selling/renting land and buildings, property management, appraisals, real estate financing, construction materials, real estate software, property listings, and mortgage services."),
    HOSPITALITY("Hospitality", "Offers accommodation, food services, tourism, event planning, hotels, restaurants, travel agencies, entertainment venues, hotel furniture, kitchen appliances, booking systems, and tourism packages."),
    ENTERTAINMENT("Entertainment", "Creates and distributes media content, including films, music, gaming, broadcasting, live performances, digital entertainment platforms, cameras, microphones, gaming consoles, streaming services, and production equipment."),
    FOOD_AND_BEVERAGE("Food and Beverage", "Produces, processes, and distributes food products, beverages, ingredients, packaging, food safety compliance, ovens, refrigerators, bottling machines, food processing lines, and agricultural products."),
    AUTOMOTIVE("Automotive", "Designs, manufactures, assembles, and services vehicles, including cars, trucks, motorcycles, parts, automotive technology, engines, transmissions, tires, car electronics, and automotive repair tools."),
    PHARMACEUTICALS("Pharmaceuticals", "Researches, develops, produces, and distributes drugs, vaccines, medical devices, clinical trials, pharmaceutical regulations, lab equipment, pill presses, vaccine storage, and medical research tools."),
    TELECOMMUNICATIONS("Telecommunications", "Provides communication services, including internet, mobile networks, broadband, satellite, VoIP, network infrastructure, routers, switches, antennas, fiber optics, and communication towers."),
    AEROSPACE("Aerospace", "Designs, builds, and maintains aircraft, spacecraft, satellites, avionics, propulsion systems, aerospace engineering, jet engines, radar systems, navigation equipment, and launch vehicles."),
    CHEMICALS("Chemicals", "Produces industrial and consumer chemicals, polymers, fertilizers, pesticides, pharmaceuticals intermediates, chemical processing, reactors, distillation equipment, storage tanks, and laboratory instruments."),
    MINING("Mining", "Extracts minerals, metals, coal, oil, gas from earth, including exploration, drilling, processing, environmental management, mining machinery, drills, crushers, conveyor belts, and extraction equipment."),
    OTHER("Other", "Miscellaneous items not fitting other categories");

    private final String name;
    private final String description;

    Industry(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}