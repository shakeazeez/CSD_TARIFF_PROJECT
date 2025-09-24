import React, { useRef, useEffect } from "react";
import { useTheme } from '../contexts/ThemeContext.jsx';

// WorldMapRoutes.jsx
// Self-contained component with simplified world map background, country lookup, and animated routes.

// Convert lat/lon to x,y in equirectangular projection
function project(lat, lon, width, height) {
  const x = ((lon + 180) / 360) * width;
  const y = ((90 - lat) / 180) * height;
  return [x, y];
}

// Generate curve between points
function makeCurvePath(x1, y1, x2, y2, curvature = 0.25) {
  const mx = (x1 + x2) / 2;
  const my = (y1 + y2) / 2;
  const dx = x2 - x1;
  const dy = y2 - y1;
  const nx = -dy;
  const ny = dx;
  const len = Math.sqrt(nx * nx + ny * ny) || 1;
  const ux = nx / len;
  const uy = ny / len;
  const dist = Math.sqrt(dx * dx + dy * dy);
  const offset = Math.max(30, dist * curvature);
  const cx = mx + ux * offset;
  const cy = my + uy * offset;
  return `M ${x1} ${y1} Q ${cx} ${cy} ${x2} ${y2}`;
}

// Simple centroid lookup for major countries
const countryCoords = {
  Singapore: { lat: 1.3521, lon: 103.8198 },
  India: { lat: 20.5937, lon: 78.9629 },
  UAE: { lat: 23.4241, lon: 53.8478 },
  Germany: { lat: 51.1657, lon: 10.4515 },
  UK: { lat: 55.3781, lon: -3.4360 },
  USA: { lat: 37.0902, lon: -95.7129 },
  China: { lat: 35.8617, lon: 104.1954 },
  Australia: { lat: -25.2744, lon: 133.7751 },
  Japan: { lat: 36.2048, lon: 138.2529 },
  SouthKorea: { lat: 35.9078, lon: 127.7669 },
  Thailand: { lat: 15.8700, lon: 100.9925 },
  Malaysia: { lat: 4.2105, lon: 101.9758 },
  Indonesia: { lat: -0.7893, lon: 113.9213 },
  Philippines: { lat: 12.8797, lon: 121.7740 },
  Vietnam: { lat: 14.0583, lon: 108.2772 },
  Brazil: { lat: -14.2350, lon: -51.9253 },
  Mexico: { lat: 23.6345, lon: -102.5528 },
  Canada: { lat: 56.1304, lon: -106.3468 },
  France: { lat: 46.2276, lon: 2.2137 },
  Italy: { lat: 41.8719, lon: 12.5674 },
  Spain: { lat: 40.4637, lon: -3.7492 },
  Netherlands: { lat: 52.1326, lon: 5.2913 },
  Belgium: { lat: 50.5039, lon: 4.4699 },
  Switzerland: { lat: 46.8182, lon: 8.2275 },
  Austria: { lat: 47.5162, lon: 14.5501 },
  Poland: { lat: 51.9194, lon: 19.1451 },
  Czech: { lat: 49.8175, lon: 15.4730 },
  Hungary: { lat: 47.1625, lon: 19.5033 },
  Romania: { lat: 45.9432, lon: 24.9668 },
  Bulgaria: { lat: 42.7339, lon: 25.4858 },
  Greece: { lat: 39.0742, lon: 21.8243 },
  Turkey: { lat: 38.9637, lon: 35.2433 },
  Russia: { lat: 61.5240, lon: 105.3188 },
  Ukraine: { lat: 48.3794, lon: 31.1656 },
  Kazakhstan: { lat: 48.0196, lon: 66.9237 },
  Uzbekistan: { lat: 41.3775, lon: 64.5853 },
  Pakistan: { lat: 30.3753, lon: 69.3451 },
  Bangladesh: { lat: 23.6850, lon: 90.3563 },
  SriLanka: { lat: 7.8731, lon: 80.7718 },
  Myanmar: { lat: 21.9162, lon: 95.9560 },
  Cambodia: { lat: 12.5657, lon: 104.9910 },
  Laos: { lat: 19.8563, lon: 102.4955 },
  Mongolia: { lat: 46.8625, lon: 103.8467 },
  Taiwan: { lat: 23.6978, lon: 120.9605 },
  HongKong: { lat: 22.3193, lon: 114.1694 },
  Macau: { lat: 22.1987, lon: 113.5439 },
  NorthKorea: { lat: 40.3399, lon: 127.5101 },
  Afghanistan: { lat: 33.9391, lon: 67.7100 },
  Iran: { lat: 32.4279, lon: 53.6880 },
  Iraq: { lat: 33.2232, lon: 43.6793 },
  SaudiArabia: { lat: 23.8859, lon: 45.0792 },
  Kuwait: { lat: 29.3117, lon: 47.4818 },
  Bahrain: { lat: 25.9304, lon: 50.6378 },
  Qatar: { lat: 25.3548, lon: 51.1839 },
  Oman: { lat: 21.5126, lon: 55.9233 },
  Yemen: { lat: 15.5527, lon: 48.5164 },
  Jordan: { lat: 30.5852, lon: 36.2384 },
  Lebanon: { lat: 33.8547, lon: 35.8623 },
  Syria: { lat: 34.8021, lon: 38.9968 },
  Israel: { lat: 31.0461, lon: 34.8516 },
  Palestine: { lat: 31.9522, lon: 35.2332 },
  Egypt: { lat: 26.0963, lon: 29.9870 },
  Libya: { lat: 26.3351, lon: 17.2283 },
  Tunisia: { lat: 33.8869, lon: 9.5375 },
  Algeria: { lat: 28.0339, lon: 1.6596 },
  Morocco: { lat: 31.7917, lon: -7.0926 },
  Portugal: { lat: 39.3999, lon: -8.2245 },
  Ireland: { lat: 53.4129, lon: -8.2439 },
  Denmark: { lat: 56.2639, lon: 9.5018 },
  Sweden: { lat: 60.1282, lon: 18.6435 },
  Norway: { lat: 60.4720, lon: 8.4689 },
  Finland: { lat: 61.9241, lon: 25.7482 },
  Estonia: { lat: 58.5953, lon: 25.0136 },
  Latvia: { lat: 56.8796, lon: 24.6032 },
  Lithuania: { lat: 55.1694, lon: 23.8813 },
  Belarus: { lat: 53.7098, lon: 27.9534 },
  Moldova: { lat: 47.4116, lon: 28.3699 },
  Georgia: { lat: 42.3154, lon: 43.3569 },
  Armenia: { lat: 40.0691, lon: 45.0382 },
  Azerbaijan: { lat: 40.1431, lon: 47.5769 },
  Kyrgyzstan: { lat: 41.2044, lon: 74.7661 },
  Tajikistan: { lat: 38.8610, lon: 71.2761 },
  Turkmenistan: { lat: 38.9697, lon: 59.5563 },
  Nepal: { lat: 28.3949, lon: 84.1240 },
  Bhutan: { lat: 27.5142, lon: 90.4336 },
  Maldives: { lat: 3.2028, lon: 73.2207 },
  Brunei: { lat: 4.5353, lon: 114.7277 },
  EastTimor: { lat: -8.8742, lon: 125.7275 },
  PapuaNewGuinea: { lat: -6.3149, lon: 143.9555 },
  Fiji: { lat: -17.7134, lon: 178.0650 },
  SolomonIslands: { lat: -9.6457, lon: 160.1562 },
  Vanuatu: { lat: -15.3767, lon: 166.9592 },
  Samoa: { lat: -13.7590, lon: -172.1046 },
  Tonga: { lat: -21.1789, lon: -175.1982 },
  Kiribati: { lat: -3.3704, lon: -168.7340 },
  Tuvalu: { lat: -7.1095, lon: 177.6493 },
  MarshallIslands: { lat: 7.1315, lon: 171.1845 },
  Micronesia: { lat: 7.4256, lon: 150.5508 },
  Palau: { lat: 7.5149, lon: 134.5825 },
  Nauru: { lat: -0.5228, lon: 166.9315 },
  NewZealand: { lat: -40.9006, lon: 174.8860 },
  Argentina: { lat: -38.4161, lon: -63.6167 },
  Chile: { lat: -35.6751, lon: -71.5430 },
  Peru: { lat: -9.1900, lon: -75.0152 },
  Colombia: { lat: 4.5709, lon: -74.2973 },
  Venezuela: { lat: 6.4238, lon: -66.5897 },
  Ecuador: { lat: -1.8312, lon: -78.1834 },
  Bolivia: { lat: -16.2902, lon: -63.5887 },
  Paraguay: { lat: -23.4425, lon: -58.4438 },
  Uruguay: { lat: -32.5228, lon: -55.7658 },
  Guyana: { lat: 4.8604, lon: -58.9302 },
  Suriname: { lat: 3.9193, lon: -56.0278 },
  FrenchGuiana: { lat: 3.9339, lon: -53.1258 },
  Panama: { lat: 8.5380, lon: -80.7821 },
  CostaRica: { lat: 9.7489, lon: -83.7534 },
  Nicaragua: { lat: 12.8654, lon: -85.2072 },
  Honduras: { lat: 15.2000, lon: -86.2419 },
  ElSalvador: { lat: 13.7942, lon: -88.8965 },
  Guatemala: { lat: 15.7835, lon: -90.2308 },
  Belize: { lat: 17.1899, lon: -88.4976 },
  Cuba: { lat: 21.5218, lon: -77.7812 },
  Haiti: { lat: 18.9712, lon: -72.2852 },
  DominicanRepublic: { lat: 18.7357, lon: -70.1627 },
  Jamaica: { lat: 18.1096, lon: -77.2975 },
  TrinidadTobago: { lat: 10.6918, lon: -61.2225 },
  Barbados: { lat: 13.1939, lon: -59.5432 },
  Bahamas: { lat: 25.0343, lon: -77.3963 },
  Iceland: { lat: 64.9631, lon: -19.0208 },
  Greenland: { lat: 71.7069, lon: -42.6043 },
  FaroeIslands: { lat: 61.8926, lon: -6.9118 },
  Albania: { lat: 41.1533, lon: 20.1683 },
  NorthMacedonia: { lat: 41.6086, lon: 21.7453 },
  Montenegro: { lat: 42.7087, lon: 19.3744 },
  Kosovo: { lat: 42.6026, lon: 20.9030 },
  Serbia: { lat: 44.0165, lon: 21.0059 },
  BosniaHerzegovina: { lat: 43.9159, lon: 17.6791 },
  Croatia: { lat: 45.1000, lon: 15.2000 },
  Slovenia: { lat: 46.1512, lon: 14.9955 },
  Slovakia: { lat: 48.6690, lon: 19.6990 },
  Luxembourg: { lat: 49.8153, lon: 6.1296 },
  Malta: { lat: 35.9375, lon: 14.3754 },
  Cyprus: { lat: 35.1264, lon: 33.4299 },
  Andorra: { lat: 42.5063, lon: 1.5218 },
  Monaco: { lat: 43.7384, lon: 7.4246 },
  SanMarino: { lat: 43.9424, lon: 12.4578 },
  VaticanCity: { lat: 41.9029, lon: 12.4534 },
  Liechtenstein: { lat: 47.1660, lon: 9.5554 },
  SouthAfrica: { lat: -30.5595, lon: 22.9375 },
  Namibia: { lat: -22.9576, lon: 18.4904 },
  Botswana: { lat: -22.3285, lon: 24.6849 },
  Zimbabwe: { lat: -19.0154, lon: 29.1549 },
  Zambia: { lat: -13.1339, lon: 27.8493 },
  Malawi: { lat: -13.2543, lon: 34.3015 },
  Mozambique: { lat: -18.6657, lon: 35.5296 },
  Tanzania: { lat: -6.3690, lon: 34.8888 },
  Kenya: { lat: -0.0236, lon: 37.9062 },
  Uganda: { lat: 1.3733, lon: 32.2903 },
  Rwanda: { lat: -1.9403, lon: 29.8739 },
  Burundi: { lat: -3.3731, lon: 29.9189 },
  Ethiopia: { lat: 9.1450, lon: 38.7379 },
  Somalia: { lat: 5.1521, lon: 46.1996 },
  Djibouti: { lat: 11.8251, lon: 42.5903 },
  Eritrea: { lat: 15.1794, lon: 39.7823 },
  Sudan: { lat: 12.8628, lon: 30.2176 },
  SouthSudan: { lat: 6.8770, lon: 31.3070 },
  Chad: { lat: 15.4542, lon: 18.7322 },
  Niger: { lat: 17.6078, lon: 8.0817 },
  Mali: { lat: 17.5707, lon: -3.9962 },
  BurkinaFaso: { lat: 12.2383, lon: -1.5616 },
  Ghana: { lat: 7.9465, lon: -1.0232 },
  Togo: { lat: 8.6195, lon: 0.8248 },
  Benin: { lat: 9.3077, lon: 2.3158 },
  Nigeria: { lat: 9.0820, lon: 8.6753 },
  Cameroon: { lat: 7.3697, lon: 12.3547 },
  CentralAfricanRepublic: { lat: 6.6111, lon: 20.9394 },
  EquatorialGuinea: { lat: 1.6508, lon: 10.2679 },
  Gabon: { lat: -0.8037, lon: 11.6094 },
  Congo: { lat: -0.2280, lon: 15.8277 },
  DemocraticRepublicCongo: { lat: -4.0383, lon: 21.7587 },
  Angola: { lat: -11.2027, lon: 17.8739 },
  Madagascar: { lat: -18.7669, lon: 46.8691 },
  Mauritius: { lat: -20.3484, lon: 57.5522 },
  Seychelles: { lat: -4.6796, lon: 55.4920 },
  Comoros: { lat: -11.8750, lon: 43.8722 },
  Mayotte: { lat: -12.8275, lon: 45.1662 },
  Reunion: { lat: -21.1151, lon: 55.5364 },
  CapeVerde: { lat: 16.0021, lon: -24.0132 },
  SaoTomePrincipe: { lat: 0.1864, lon: 6.6131 },
  GuineaBissau: { lat: 11.8037, lon: -15.1804 },
  Guinea: { lat: 9.9456, lon: -9.6966 },
  SierraLeone: { lat: 8.4606, lon: -11.7799 },
  Liberia: { lat: 6.4281, lon: -9.4295 },
  IvoryCoast: { lat: 7.5400, lon: -5.5471 },
  Senegal: { lat: 14.4974, lon: -14.4524 },
  Gambia: { lat: 13.4432, lon: -15.3101 },
  Mauritania: { lat: 21.0079, lon: -10.9408 },
  WesternSahara: { lat: 24.2155, lon: -12.8858 },
  Lesotho: { lat: -29.6099, lon: 28.2336 },
  Eswatini: { lat: -26.5225, lon: 31.4659 },
  AmericanSamoa: { lat: -14.2710, lon: -170.1322 },
  CookIslands: { lat: -21.2367, lon: -159.7777 },
  FrenchPolynesia: { lat: -17.6797, lon: -149.4068 },
  Guam: { lat: 13.4443, lon: 144.7937 },
  NewCaledonia: { lat: -20.9043, lon: 165.6180 },
  NorthernMarianaIslands: { lat: 17.3308, lon: 145.3847 },
  PuertoRico: { lat: 18.2208, lon: -66.5901 },
  USVirginIslands: { lat: 18.3358, lon: -64.8963 },
  Aruba: { lat: 12.5211, lon: -69.9683 },
  Curacao: { lat: 12.1696, lon: -68.9900 },
  SintMaarten: { lat: 18.0425, lon: -63.0548 },
  TurksCaicos: { lat: 21.6940, lon: -71.7979 },
  CaymanIslands: { lat: 19.5135, lon: -80.5660 },
  Bermuda: { lat: 32.3078, lon: -64.7505 },
  Gibraltar: { lat: 36.1408, lon: -5.3536 },
  Guernsey: { lat: 49.4657, lon: -2.5853 },
  IsleMan: { lat: 54.2361, lon: -4.5481 },
  Jersey: { lat: 49.2144, lon: -2.1312 },
  FalklandIslands: { lat: -51.7963, lon: -59.5236 },
  SouthGeorgia: { lat: -54.4296, lon: -36.5879 },
  SaintHelena: { lat: -24.1435, lon: -10.0307 },
  AscensionIsland: { lat: -7.9467, lon: -14.3559 },
  TristanDaCunha: { lat: -37.1052, lon: -12.2777 },
  BouvetIsland: { lat: -54.4208, lon: 3.3464 },
  BritishIndianOceanTerritory: { lat: -6.3432, lon: 71.8765 },
  FrenchSouthernTerritories: { lat: -49.2804, lon: 69.3486 },
  HeardIsland: { lat: -53.0818, lon: 73.5042 },
  McDonaldIslands: { lat: -53.0818, lon: 73.5042 },
  Antarctica: { lat: -82.8628, lon: 135.0000 },
};

export default function WorldMapRoutes({
  width = 1920,
  height = 1080,
  countryList = Object.keys(countryCoords),
  type = "plane",
  color = "#436083ff",
  speed = 0.01,
  background = true,
}) {
  const svgRef = useRef(null);
  const animRef = useRef([]);
  const { colors, isDark } = useTheme();

  // Convert countries → coordinates
  const allStops = countryList.map((c) => ({ ...countryCoords[c], name: c })).filter((c) => c.lat && c.lon);

  // Build random routes between countries for animation
  const pathData = [];
  const numRoutes = Math.min(15, Math.floor(allStops.length / 2)); // Limit routes for performance

  for (let i = 0; i < numRoutes; i++) {
    const fromIdx = Math.floor(Math.random() * allStops.length);
    let toIdx = Math.floor(Math.random() * allStops.length);
    while (toIdx === fromIdx) toIdx = Math.floor(Math.random() * allStops.length);

    const from = allStops[fromIdx];
    const to = allStops[toIdx];
    const [x1, y1] = project(from.lat, from.lon, width, height);
    const [x2, y2] = project(to.lat, to.lon, width, height);
    const d = makeCurvePath(x1, y1, x2, y2, 0.15);
    pathData.push({ d, from, to });
  }

  // Filter stops to only include countries used in routes
  const routeCountries = new Set();
  pathData.forEach(p => {
    routeCountries.add(p.from.name);
    routeCountries.add(p.to.name);
  });
  const stops = allStops.filter(s => routeCountries.has(s.name));

  useEffect(() => {
    const svg = svgRef.current;
    if (!svg) return;
    animRef.current.forEach((a) => a.cancel && a.cancel());
    animRef.current = [];

    pathData.forEach((p, idx) => {
      const pathEl = svg.querySelector(`#route-path-${idx}`);
      const markerEl = svg.querySelector(`#route-marker-${idx}`);
      if (!pathEl || !markerEl) return;

      const pathLength = pathEl.getTotalLength();
      let pos = 0;
      let cancelled = false;

      function frame() {
        if (cancelled) return;
        pos += speed * 6;
        if (pos > pathLength) pos = 0;
        const pt = pathEl.getPointAtLength(pos);
        markerEl.setAttribute("cx", pt.x);
        markerEl.setAttribute("cy", pt.y);
        requestAnimationFrame(frame);
      }

      requestAnimationFrame(frame);

      animRef.current.push({ cancel: () => (cancelled = true) });
    });

    return () => animRef.current.forEach((a) => a.cancel());
  }, [pathData, speed]);

  return (
    <div className={`w-full h-full ${background ? 'fixed inset-0 pointer-events-none z-0' : 'relative'}`}>
      <svg
        ref={svgRef}
        width="100%"
        height="100%"
        viewBox="360 202 1200 675"
        className="absolute inset-0"
        style={{ opacity: background ? 0.6 : 1 }}
      >
        {/* World map background */}
        <defs>
          <radialGradient id="oceanGradient" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stopColor={isDark ? "#0f172a" : "#e2e8f0"} />
            <stop offset="100%" stopColor={isDark ? "#1e293b" : "#cbd5e1"} />
          </radialGradient>
          <filter id="glow">
            <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
            <feMerge>
              <feMergeNode in="coloredBlur"/>
              <feMergeNode in="SourceGraphic"/>
            </feMerge>
          </filter>
        </defs>

        {/* Ocean background */}
        <rect x="0" y="0" width={width} height={height} fill="url(#oceanGradient)" opacity="0.9" />

        {/* Simplified continent outlines */}
        <path
          d="M200 150 Q300 120 400 140 Q500 160 600 150 Q700 140 800 160 Q850 180 900 170 Q950 160 1000 180 Q1100 200 1200 190 Q1300 180 1400 200 Q1500 220 1600 210 Q1650 200 1700 220 L1700 400 Q1600 420 1500 410 Q1400 400 1300 420 Q1200 440 1100 430 Q1000 420 900 440 Q800 460 700 450 Q600 440 500 460 Q400 480 300 470 Q200 460 200 480 Z"
          fill={isDark ? "#0a1934ff" : "#94a3b8"}
          stroke={isDark ? "#4a5568" : "#64748b"}
          strokeWidth="1"
          opacity="0.4"
        />

        {/* Draw animated routes */}
        {pathData.map((p, idx) => (
          <g key={idx}>
            <path
              id={`route-path-${idx}`}
              d={p.d}
              stroke={color}
              strokeWidth="2"
              strokeDasharray="8 8"
              fill="none"
              opacity="0.6"
              filter="url(#glow)"
            />
            <circle
              id={`route-marker-${idx}`}
              r="6"
              fill={color}
              opacity="0.9"
              filter="url(#glow)"
            />
          </g>
        ))}

        {/* Country markers */}
        {stops.map((s, idx) => {
          const [x, y] = project(s.lat, s.lon, width, height);
          return (
            <g key={`stop-${idx}`}>
              <circle
                cx={x}
                cy={y}
                r="3"
                fill={isDark ? "#60A5FA" : "#7cc47fff"}
                stroke={isDark ? "#fff" : "#000"}
                strokeWidth="0.5"
                opacity="0.9"
              />
            </g>
          );
        })}
      </svg>
    </div>
  );
}
