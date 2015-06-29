# license-plates

Two versions of the same thing
LicensePlateGenerator - single threaded implementation
LicensePlatePipeline - multiple threaded implementation
	- the pipeline works as follows:
		* a producer generates all the possible 3 letter permutations with repetitions and populates a queue
		* three filters consume the letters from the queue and pass to the filtered queue only the valid entries
		* eight generators consume the filtered letters and create the appropriate license plates for their respective regions
			and populate a final license plates queue
		* a writer consumes the license plates from that queue and writes them to disk