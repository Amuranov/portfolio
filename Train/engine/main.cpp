#include "graph.hpp"
#include "../solver/Solver.hpp"


#include <iostream>
#include <vector>



#define TIMEWAIT 3			// le temps d'attente minimum dans une gare desservie
#define SLOW 1				// nombre de train lent
#define FAST 0				// nombre de train rapide
#define TRAIN (SLOW+FAST)	// nombre de train
#define TIMESLOT 30			// nombre de minutes dans la plage horaire
#define STATION 4			// nombre de gare (voir la map)
#define TIMEDURATION 5		// duree maximal des voyages direct
#define TIMEWINDOW 5		// frequence des trains direct


Solver solver;
vec<Lit> literals;
int segment[STATION][STATION][TRAIN][TIMESLOT];
int gares[TRAIN][STATION][TIMESLOT];



// initialisation des 2 matrices
void init_variables() {
    // initialisation de segment
    for (int g1=0 ; g1 < STATION; g1++) {
        for (int g2=0; g2 < STATION ; g2++){
            for (int t=0; t < TRAIN; t++) {
                for (int i=0; i < TIMESLOT; i++) {
                        segment[g1][g2][t][i] = solver.newVar();
                }
            }
        }
    }
    // initialisation de gares
    for (int t=0; t< TRAIN ; t++){
        for(int s = 0; s<STATION;s++){
            for(int k =0 ; k<TIMESLOT;k++){
                gares[t][s][k] = solver.newVar();
            }
        }
    }
}


// Fonction utiles pour alléger les contraintes
bool trainAndGareCompatibles(Graph *map, int gare, int train){
    bool res = false;
    // Si nous avons un train de type Slow, il est compatible avec tout
    if (train < SLOW){
        res = true;
    }
    else if (map->get_type(gare) == Big ) {
        res = true;
    }
    return res;
}

// ======== Contraintes explicites ==========
// Contrainte de deux Trains sur le même segment
void contrainte2(Graph *map){
	for (int t1 = 0; t1 < TRAIN ; t1++) {
			for (int t2 = 0; t2 < TRAIN; t2++){
				if (t1 != t2) {
					for (int i=0; i < TIMESLOT; i++) {
						for (int g1=0 ; g1 < STATION; g1++) {
							for (int g2=0; g2 < STATION ; g2++) {
								if (map->get_duration(g1,g2) > 0) {
									solver.addBinary(~Lit(segment[g1][g2][t1][i]),~Lit(segment[g1][g2][t2][i]));
								}
							}
						}
					}
				}

			}

		}
}


// contrainte de trains fast dans les gares big, et des trains slow dans toutes les gares
void contrainte3(Graph *map){
    for (int g=0 ; g < STATION; g++) {
        for (int g2=0; g2 < STATION;g++){
            for (int t = 0; t < TRAIN; t++) {
                for (int i=0; i < TIMESLOT; i++) {
                    if (trainAndGareCompatibles(map, g, t) and (trainAndGareCompatibles(map, g2, t))) {
                        solver.addBinary(Lit(segment[g][g2][t][i]), Lit(segment[g][g2][t][i]));
                    }
                }
            }
        }
    }
}

// Contrainte forçant le train à attendre au moins TIMEWAIT à chaque station
void contrainte4(Graph *map){
    for(int t1=0; t1 < TIMEWINDOW ;t1++){
        for(int t2=0; t2 < TIMEWINDOW ;t2++ ) {
            if( (t1 <= t1+TIMEWAIT) and (t1+TIMEWAIT<=t2) ){
                for (int g = 0; g < STATION; g++) {
                    for (int T = 0; T < TRAIN; T++) {
                        literals.clear();
                        literals.push(Lit(gares[T][g][t2]));
                        solver.addClause(literals);
                    }
                }
            }
        }
    }
}

// Contrainte sur le temps de trajet constants
void contrainte5(Graph *map){
    for(int t1=0; t1 < TIMEWINDOW ;t1++){
        for(int t2=0; t2 < TIMEWINDOW ;t2++ ) {
            if( t2 <= t1 ){
                for (int g = 0; g < STATION; g++) {
                    for (int T = 0; T < TRAIN; T++) {
                        solver.addBinary(Lit(gares[T][g][t1]), ~Lit(gares[T][g][t2]));
                    }
                }
            }
        }
    }
}

// Contrainte forçant une gare à ne pas pouvoir accueillir plus que ce que qu'elle peut.
void contrainte6(Graph *map){

}


// ================ Contraintes implicites ================

// Contrainte interdisant à 1 train à se trouver dans 2 gares différentes à un même moment
void contrainteImplicite1(Graph *map){
    for(int t=0; t < TRAIN ; t++){
        for(int s1=0; s1 < STATION ; s1++) {
            for (int s2=0; s2 < STATION ; s2++){
                if( s1 != s2 ){
                    for (int T = 0; T < TIMESLOT; T++) {
                        solver.addBinary(Lit(gares[t][s1][T]), ~Lit(gares[t][s2][T]));
                    }
                }
            }
        }
    }
}
// Contrainte interdisant à 1 train à se trouver sur 2 voies différentes à un même moment
void contrainteImplicite2(Graph *map){
    for (int a1 = 0; a1 < STATION; a1++){
        for (int a2 = 0; a2 < STATION; a2++){
            for (int b1 = 0; b1 < STATION; b1++){
                for (int b2 = 0; b2 < STATION ; b2++){
                    if( (a1=!a2) and (a1=!b1) and (a1!=b2) and (a2!=b1) and (a2 != b2) and (b1 != b2)){
                        for (int T = 0; T < TRAIN; T++){
                            for (int t = 0; t < TIMESLOT; t++){
                                solver.addBinary(Lit(segment[a1][a2][T][t]),~Lit(segment[b1][b2][T][t]));
                            }
                        }
                    }
                }
            }
        }
    }
}

// Contrainte interdisant à un train à se trouver en gare et sur une voie à un même moment
void contrainteImplicite3(Graph *map){
    for (int s1 = 0; s1 < STATION ; s1++){
        for (int s2 = 0; s2 < STATION ; s2++){
            for (int s3 = 0; s3 < STATION ; s3++){
                if((s1 != s2) and (s1 != s3) and (s2 != s3)){
                    for (int T = 0; T < TRAIN ; T++){
                        for (int t = 0; t < TIMESLOT; t++ ){
                            solver.addBinary(Lit(gares[T][s1][t]),~Lit(segment[s2][s3][T][t]));
                        }
                    }
                }
            }
        }
    }
}

//
int main() {
	// ---------- Map ---------- //


	Graph* map;
	map = Graph::parse((char *)"maps/cycle.txt"); // you can change the map file here !!
	map->print();
	assert(STATION == map->get_size());



	// ---------- Variables ---------- //

    init_variables();

	// ---------- Constraints ---------- //

    contrainte2(map);
    //contrainte3(map);
    contrainte4(map);
    contrainte5(map);
    contrainteImplicite1(map);
    //contrainteImplicite2(map);
    contrainteImplicite3(map);



	// ---------- Solver ---------- //

	printf("\n\n");
	solver.solve();
	printf("\n");



	// ---------- Printer ---------- //

	if (!solver.okay()) {
		printf("\nNO\n");
	}
	else {
		printf("\nYES\n");
	}



	// ---------- Delete ---------- //

	delete map;
	return EXIT_SUCCESS;
}
