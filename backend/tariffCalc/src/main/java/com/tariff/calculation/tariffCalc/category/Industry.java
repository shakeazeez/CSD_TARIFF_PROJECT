package com.tariff.calculation.tariffCalc.category;

public enum Industry {
    AGRICULTURE("Agriculture", "Involves farming, crop cultivation, livestock breeding, aquaculture, forestry, horticulture, and related activities for food production, fiber, biofuels, and raw materials. Includes tractors, fertilizers, seeds, irrigation systems, dairy farming, poultry, fishing gear, timber harvesting, and agricultural machinery."),
    MANUFACTURING("Manufacturing", "Industrial production and assembly of mechanical parts, metal fabrication, plastic molding, textile production, machinery manufacturing, industrial equipment, factory automation, production line systems, quality control instruments, welding equipment, cutting tools, and general manufacturing machinery."),
    RETAIL("Retail", "Consumer goods sales, shopping centers, department stores, fashion retailers, electronics stores, home goods, sporting goods, books, toys, jewelry, clothing, accessories, point-of-sale systems, retail displays, shopping carts, barcode scanners, and customer service equipment."),
    HEALTHCARE("Healthcare", "Medical treatment services, hospitals, clinics, diagnostic imaging, surgical procedures, patient care, medical examinations, health monitoring, therapeutic treatments, dental care, nursing services, medical records systems, hospital equipment, medical devices, and patient management services."),
    TECHNOLOGY("Technology", "Computer systems, software development, IT infrastructure, data centers, cloud computing, artificial intelligence, machine learning, cybersecurity, computer hardware, servers, networking devices, programming tools, digital platforms, and information technology services."),
    FINANCE("Finance", "Manages banking, investments, insurance, financial planning, risk assessment, lending, payments, regulatory compliance, ATMs, trading platforms, credit cards, card readers, payment terminals, POS devices, financial software, blockchain, and cryptocurrency services."),
    EDUCATION("Education", "Delivers teaching, training, research, and learning services through schools, universities, online courses, vocational programs, educational materials, textbooks, e-learning platforms, laboratory equipment, and educational software."),
    CONSTRUCTION("Construction", "Builds infrastructure, residential and commercial buildings, roads, bridges, using engineering, project management, construction techniques, cranes, bulldozers, concrete mixers, scaffolding, surveying equipment, and building materials."),
    TRANSPORTATION("Transportation", "Handles logistics, shipping, freight, passenger travel, vehicle maintenance, traffic management, transportation safety, trucks, ships, airplanes, railways, GPS systems, fleet management, and transportation hubs."),
    ENERGY("Energy", "Produces, distributes, and manages power from sources like oil, gas, coal, nuclear, solar, wind, hydroelectric, energy efficiency, power plants, transformers, solar panels, wind turbines, batteries, and smart grids."),
    REAL_ESTATE("Real Estate", "Property development, residential buildings, commercial properties, land acquisition, real estate investment, property management systems, building appraisals, real estate marketing, property sales, rental management, real estate finance, and property valuation tools."),
    HOSPITALITY("Hospitality", "Hotels, restaurants, tourism services, accommodation booking, food service, travel planning, event hosting, guest services, hotel management systems, restaurant equipment, tourism packages, entertainment venues, and hospitality customer service."),
    ENTERTAINMENT("Entertainment", "Creates and distributes media content, including films, music, gaming, broadcasting, live performances, digital entertainment platforms, cameras, microphones, gaming consoles, streaming services, and production equipment."),
    FOOD_AND_BEVERAGE("Food and Beverage", "Produces, processes, and distributes food products, beverages, ingredients, packaging, food safety compliance, ovens, refrigerators, bottling machines, food processing lines, agricultural products, table salt, food-grade sodium chloride, culinary seasonings, spices, food additives, and food preservation materials."),
    AUTOMOTIVE("Automotive", "Designs, manufactures, assembles, and services vehicles, including cars, trucks, motorcycles, parts, automotive technology, engines, transmissions, tires, car electronics, and automotive repair tools."),
    PHARMACEUTICALS("Pharmaceuticals", "Drug manufacturing, pharmaceutical research, medicine production, vaccine development, clinical trials, pharmaceutical ingredients, pill manufacturing, drug testing equipment, laboratory analysis, pharmaceutical packaging, medication distribution, and drug discovery processes."),
    TELECOMMUNICATIONS("Telecommunications", "Communication networks, internet services, mobile phone systems, broadband infrastructure, telephone services, data transmission, wireless communication, satellite communication, fiber optic networks, communication towers, network equipment, and telecommunication devices."),
    AEROSPACE("Aerospace", "Designs, builds, and maintains aircraft, spacecraft, satellites, avionics, propulsion systems, aerospace engineering, jet engines, radar systems, navigation equipment, and launch vehicles."),
    CHEMICALS("Chemicals", "Produces industrial and consumer chemicals, polymers, fertilizers, pesticides, pharmaceuticals intermediates, chemical processing, reactors, distillation equipment, storage tanks, laboratory instruments, pure chemical compounds, sodium chloride, industrial salts, chemical reagents, sulphur, lime, cement, and basic chemical raw materials."),
    MINING("Mining", "Involves extraction of natural resources such as minerals, metals, coal, oil, and gas. Includes exploration, drilling, excavation, ore processing, mining machinery, crushers, conveyor belts, and environmental management systems."),
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