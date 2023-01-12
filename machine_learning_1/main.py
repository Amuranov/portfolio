from sklearn import tree, metrics
from sklearn.metrics import classification_report, confusion_matrix
from sklearn.externals.six import StringIO  # Allow to read a string as a file
import matplotlib.pyplot as plt
import pandas as pd         # Read data from .cvs
import pydotplus

def tree_to_png(model, fname):
    dot_data = StringIO()
    tree.export_graphviz(model, out_file=dot_data, filled=True, rounded=False, special_characters=True,)
    graph = pydotplus.graph_from_dot_data(dot_data.getvalue())
    graph.write_png('%s.png' % fname)

def get_plot(x, y_train, y_test, fname, meta_parameter):
    """ plotted as showed in class """
    plt.plot(x, y_train, 'b', label="training set error")
    plt.plot(x, y_test, 'r', label="testing set error")
    plt.xlabel(meta_parameter)
    plt.ylabel("errors")
    plt.legend()
    plt.savefig(fname + ".png")
    plt.clf()

def get_evaluation(y_trainSet, y_testSet, y_pred_train, y_pred_test, fname):
    """ Create a txt file with evaluation in it """
    with open(fname, 'w') as f:
        f.write("Training set evaluation:")
        f.write(str(confusion_matrix(y_trainSet, y_pred_train)) + "\n" + str(classification_report(y_trainSet, y_pred_train)))
        f.write("Training set error: " + str(1 - metrics.accuracy_score(y_trainSet, y_pred_train)) + "\n\n")
        f.write("Test set Evaluation:")
        f.write(str(confusion_matrix(y_testSet, y_pred_test))+ "\n" + str(classification_report(y_testSet, y_pred_test)))
        f.write("Test set Error: " + str(1 - metrics.accuracy_score(y_testSet, y_pred_test)))

def get_DT_MaxDepth(x_trainSet, x_testSet, y_trainSet,  y_testSet, max_depth):
    """ Allows to get a decision tree with a given max depth """
    clf = tree.DecisionTreeClassifier(max_depth=max_depth, criterion="entropy")
    clf.fit(x_trainSet, y_trainSet)
    
    # Test prediction
    y_pred_test = clf.predict(x_testSet)
    y_pred_train = clf.predict(x_trainSet)

    # Evaluation in txt file
    get_evaluation(y_trainSet, y_testSet, y_pred_train, y_pred_test, 'evaluation_max_depth_of_'+str(max_depth)+'.txt')
    
    # export to png
    tree_to_png(clf, 'dt_max_depth_of_'+str(max_depth))

def get_DT_max_leaf_nodes(x_trainSet, x_testSet, y_trainSet,  y_testSet, max_leaf_nodes):
    """ Allows to get a decision tree xith a given leaf nodes limit """
    clf = tree.DecisionTreeClassifier(max_leaf_nodes=max_leaf_nodes, criterion="entropy")
    clf.fit(x_trainSet, y_trainSet)

    # Test prediction
    y_pred_test = clf.predict(x_testSet)
    y_pred_train = clf.predict(x_trainSet)

    # Evaluation in txt file
    get_evaluation(y_trainSet, y_testSet, y_pred_train, y_pred_test, 'evaluation_max_leaf_nodes_of_'+str(max_leaf_nodes)+'.txt')

    # export to png
    tree_to_png(clf, 'dt_max_leaf_nodes_of_'+str(max_leaf_nodes))

def get_error_plot_max_depth(x_trainSet, x_testSet, y_trainSet, y_testSet, fname):
    """ get the error linked to max depth """
    x, ytrained, ytested = list(), list(), list()
    for k in range(1, 101):
        x.append(k)
        clf = tree.DecisionTreeClassifier(criterion="entropy", max_depth=k)
        clf = clf.fit(x_trainSet, y_trainSet)

        #On train set
        pred_train = clf.predict(x_trainSet)
        ytrained.append(1-metrics.accuracy_score(y_trainSet, pred_train))
        #On test set
        pred_test = clf.predict(x_testSet)
        ytested.append(1-metrics.accuracy_score(y_testSet, pred_test))

    get_plot(x, ytrained, ytested, fname, 'max_depth')

def get_error_max_leaf_nodes(x_trainSet, x_testSet, y_trainSet, y_testSet, fname):
    """ get the error linked to max leaf nodes """
    x, ytrained, ytested  = list(), list(), list()
    for k in range(2, 100):
        x.append(k)
        clf = tree.DecisionTreeClassifier(criterion="entropy", max_leaf_nodes=k)
        clf = clf.fit(x_trainSet, y_trainSet)

        # On train Set
        pred_train = clf.predict(x_trainSet)
        ytrained.append(1-metrics.accuracy_score(y_trainSet, pred_train))
        # On test Set
        pred_test = clf.predict(x_testSet)
        ytested.append(1-metrics.accuracy_score(y_testSet, pred_test))

    get_plot(x, ytrained, ytested, fname, 'max_leaf_nodes')

def main():
    """ Creates an answer for every question """
    print("====== Beginning ======")
    # Reading an dividing data
    train_data = pd.read_csv("Adult_train.csv", sep=',')
    test_data = pd.read_csv("Adult_test.csv", sep=',')
    x_train, y_train = train_data.drop("salary", axis=1),  train_data["salary"]
    x_test, y_test = test_data.drop("salary", axis=1), test_data["salary"]

    # Section 1
    print("Creation tree of depth 3")
    get_DT_MaxDepth(x_train, x_test, y_train, y_test, 3)

    # Section 2
    print("Creation of tree with max leaf nodes of 5")
    get_DT_max_leaf_nodes(x_train, x_test, y_train,  y_test, 5)

    # Section 3
    print("Plotting error max depth")
    get_error_plot_max_depth(x_train, x_test, y_train, y_test, "error_question_3")

    # Section 4
    print("Calculating error max leaf node")
    get_error_max_leaf_nodes(x_train, x_test, y_train, y_test, "error_question_4")

    # Section 5
    print("Creation of depth 5 tree")
    get_DT_MaxDepth(x_train, x_test, y_train, y_test, 5)
    print("Creation of 33 leaf tree")
    get_DT_max_leaf_nodes(x_train, x_test, y_train, y_test, 33)

    print("========== END ==========")


if __name__ == "__main__":
    main()
