// ⚽️ Données mockées : simulation de ce que renverra le backend Java
const MOCK_POINTS = [
  {
    id: 1,
    name: "City Stade Paris 15",
    lat: 48.841,
    lng: 2.29,
    sports: ["FOOT", "BASKET"],
    status: "TEAM_RED",
    teamName: "Red Falcons",
    influence: 65,
  },
  {
    id: 2,
    name: "Parc des Buttes-Chaumont",
    lat: 48.88,
    lng: 2.381,
    sports: ["COURSE", "MUSCU"],
    status: "NEUTRAL",
    teamName: null,
    influence: 40,
  },
  {
    id: 3,
    name: "Gymnase Saint-Mandé",
    lat: 48.845,
    lng: 2.42,
    sports: ["VOLLEY", "MUSCU"],
    status: "TEAM_BLUE",
    teamName: "Blue Wolves",
    influence: 80,
  },
];

// 🔁 Fonction qui simule un appel API asynchrone
export async function getPoints() {
  // On simule un petit délai réseau
  await new Promise((resolve) => setTimeout(resolve, 300));
  return MOCK_POINTS;
}

// 💡 Plus tard, tu pourras remplacer par du vrai HTTP avec Axios, par ex. :
// import axios from "axios";
// export async function getPoints() {
//   const res = await axios.get("http://localhost:8080/api/points");
//   return res.data;
// }