## Motif-based Approximate Counting via Hypergraphs (MACH)

This is the modular code for approximate satisfiability counting for First Order defininite clauses following the original paper - *Das et al., AAAI 2019* [1]. This code implements the algorithm **M**otif-based **A**pproximate **C**ounting via **H**ypergraphs (MACH)
as a plug & play module. It can either be used independently as an approximate counting library for definite clauses of your choice *OR* it can be integrated with your favourite Probabilistic Logic Learner or Lifted Inference code (preferably java code), to make them scalable. For technical details about the algorithm itself please refer to the cited paper. 


### Dependencies
There are several library dependencies that needs to be resolved. (The *jar* files are provided in a separate folder with the code as well)
1. **Commons Collections 4.0**
2. **Google Guava 25.0** ([Link](https://opensource.google.com/projects/guava))
3. **Hypergraph DB 1.3** ([Link](http://www.hypergraphdb.org/))
4. **AUC library** (Use provided jar file)


[1]: M. Das, D. S. Dhami, G. Kunapuli, K. Kersting & S. Natarajan. [Fast Relational Probabilistic Inference and Learning Approximate Counting via Hypergraphs](https://starling.utdallas.edu/assets/pdfs/AAAI18_HyperGraphApproxCount.pdf). *AAAI* 2019
