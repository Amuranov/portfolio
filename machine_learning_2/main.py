import matplotlib.pyplot as plot
import utils as utils
import tsne as tsne
from sklearn import preprocessing
from sklearn.cluster import KMeans



### utils ###
def get_inerty(given_data,number_of_clusters):
    cluster = KMeans(n_clusters=number_of_clusters,init="k-means++",random_state=42)
    cluster.fit_predict(given_data)
    return(cluster.inertia_)

def normalize(prpxlty):
    data = utils.load_HDR_data()
    x_train = data["X"]
    x_normalized = preprocessing.StandardScaler().fit_transform(x_train)
    country_names = data["country_names"]
    return(country_names ,x_normalized, tsne.tsne(x_normalized, perplexity=prpxlty))

def clustering(number_of_clusters, x_normalized, x_normalized_reduced, country_names): 
    for i in range(2, number_of_clusters + 1):
        kmeans          = KMeans(n_clusters=i, init="k-means++")
        clustered_data  = kmeans.fit_predict(x_normalized)

        utils.show_annotated_clustering(x_normalized_reduced, clustered_data, country_names)

        closest_indices = utils.find_closest_instances_to_kmeans(x_normalized, kmeans)[1]
        print("===============",str(i) + " clusters:", " ===============")
        print_result(closest_indices, country_names)

def print_result(closest_indices, country_names):
    
    for i in closest_indices:
        print("\t" + country_names[i])
    plot.show()

def plot_choice(number_of_clusters, x_normalized):
    evol = {}
    plot.figure("Evolution of W against K")
    plot.xlabel("K"), plot.ylabel("W")
    
    for i in range(2, number_of_clusters + 1):
        evol[i] = get_inerty(x_normalized, i)

    plot.plot(list(evol.keys()), list(evol.values()))
    plot.show()


def main():
    number_of_clusters = 10
    perplexity         = 6

    ### TASK 1 ###
    country_names, x_normalized, x_normalized_reduced = normalize(perplexity)

    ### TASK 2 ###
    clustering(number_of_clusters, x_normalized, x_normalized_reduced, country_names)

    ### Defending number of cluster ###
    plot_choice(number_of_clusters, x_normalized)



if __name__ == "__main__":
    main()
