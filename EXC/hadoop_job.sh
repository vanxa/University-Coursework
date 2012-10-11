#!/bin/bash

# Before starting the jobs, enter the local data directory and the hadoop streaming jar location
loc_data_dir=
hadoop_dir=


function job1_task1 ()
{
	echo "Starting task 1 with data:$1"
	if [ -z $1 ]; then
		DATA='input_small'
	elif [ $1 == 's' ]; then
		DATA='input_small'
	elif [ $1 == 'l' ]; then
		DATA='input_large'
	fi
	hadoop fs -rm $loc_data_dir/features/*
	hadoop fs -rmr $loc_data_dir/data/output
	hadoop jar $hadoop_dir/hadoop-0.20.2-streaming.jar \
	-input $loc_data_dir/$DATA \
	-output $loc_data_dir/output \
	-mapper feat_extract_map.py \
	-file job1/feat_extract_map.py \
	-reducer feat_extract_red.py \
	-file job1/feat_extract_red.py \
	-jobconf mapred.job.name="Dreamland_task1" \
	-jobconf mapred.map.tasks=115
	if [ -e train_features ]; then
		rm train_features
	fi
	hadoop fs -getmerge $loc_data_dir/output train_features
	hadoop fs -copyFromLocal result_job1_task1 $loc_data_dir/features
}

function job1_task2 ()
{
	hadoop fs -rmr $loc_data_dir/output
	hadoop jar $hadoop_dir/hadoop-0.20.2-streaming.jar \
	-input $loc_data_dir/features \
	-output $loc_data_dir/output \
	-mapper feat_count_map.py \
	-file job1/feat_count_map.py \
	-reducer feat_count_red.py \
	-file job1/feat_count_red.py \
	-jobconf mapred.job.name="Dreamland_task2" \
	-jobconf mapred.map.tasks=80
	if [ -e counts ]; then
		rm counts
	fi
	hadoop fs -getmerge $loc_data_dir/output counts
}

function job1_task3 ()
{
	hadoop fs -rmr $loc_data_dir/output
	hadoop jar $hadoop_dir/hadoop-0.20.2-streaming.jar \
	-input $loc_data_dir/features \
	-output $loc_data_dir/output \
	-mapper feat_probs_map.py \
	-file job1/feat_probs_map.py \
	-reducer feat_probs_red.py \
	-file job1/feat_probs_red.py \
	-file counts \
	-jobconf mapred.job.name="Dreamland_task3" \
	-jobconf mapred.map.tasks=120

	if [ -e model ]; then
		rm model
	fi
	hadoop fs -getmerge $loc_data_dir/output model
	hadoop fs -rmr $loc_data_dir/output
}

function job2 ()
{
	hadoop fs -rmr $loc_data_dir/output
	hadoop jar $hadoop_dir/hadoop-0.20.2-streaming.jar \
	-input $loc_data_dir/input_test \
	-output $loc_data_dir/output \
	-mapper counter_map.py \
	-file job2/counter_map.py \
	-reducer counter_red.py \
	-file job2/counter_red.py \
	-jobconf mapred.line.input.format.linespermap=10 \
	-jobconf mapred.job.name="Dreamland_job2" \
	-jobconf mapred.map.tasks=20

	if [ -e test_words ]; then
		rm test_words
		hadoop fs -rm $loc_data_dir/test_dat/*
	fi
	hadoop fs -getmerge $loc_data_dir/output test_words
	hadoop fs -copyFromLocal test_words $loc_data_dir/test_dat/
	hadoop fs -rmr $loc_data_dir/output

}

function job3 ()
{
	hadoop fs -rmr $loc_data_dir/output
	hadoop jar $hadoop_dir/hadoop-0.20.2-streaming.jar \
	-input $loc_data_dir/test_dat \
	-output $loc_data_dir/output \
	-mapper word_score_map.py \
	-file job3/word_score_map.py \
	-reducer word_score_red.py \
	-file job3/word_score_red.py \
	-file model \
	-jobconf mapred.job.name="Dreamland_job3" \
	-jobconf mapred.map.tasks=10

	if [ -e word_scores ]; then
		rm word_scores
	fi
	hadoop fs -getmerge $loc_data_dir/output word_scores
	hadoop fs -rmr $loc_data_dir/output

}

function job4 ()
{
	hadoop fs -rmr $loc_data_dir/output
	hadoop jar $hadoop_dir/hadoop-0.20.2-streaming.jar \
	-input $loc_data_dir/test_dat \
	-output $loc_data_dir/output \
	-mapper case_correction_map.py \
	-file job4/case_correction_map.py \
	-reducer case_correction_red.py \
	-file job4/case_correction_red.py \
	-file word_scores \
	-jobconf mapred.job.name="Dreamland_job4" \
	-jobconf mapred.map.tasks=3

	if [ -e final ]; then
		rm final
	fi
	hadoop fs -getmerge $loc_data_dir/output final
	hadoop fs -rmr $loc_data_dir/output
}

function cleanup ()
{
	hadoop fs -rm $loc_data_dir/features/*
	hadoop fs -rm $loc_data_dir/test_dat/*
	hadoop fs -rmr $loc_data_dir/output
	if [ -e model ]; then
		rm model
	fi
	if [ -e counts ]; then
		rm counts
	fi
	if [ -e train_features ]; then
		rm train_features
	fi
	if [ -e test_words ]; then
		rm test_words
	fi
	if [ -e word_scores ]; then
		rm word_scores
	fi
	if [ -e final ]; then
		rm final
	fi
}


function do_all ()
{
	start_time=$(date +%s)
	cleanup
	job1_task1 $1
	job1_task2
	job1_task3
	job2
	job3
	job4
	finish_time=$(date +%s)
	echo "Time duration: $(($((finish_time - start_time))/60)) mins."
}

if [ -z "$1" ]; then
	echo "No input passed, defaulting to all"
	do_all 's'
elif [ $1 == 0 ]; then
	do_all $2	
elif [ $1 == 1 ]; then
	if [ -z "$2" ]; then
		echo "No task selected for job 1; defaulting to all"
		job1_task1 "l"
		job1_task2
		job1_task3
	elif [ $2 == 1 ]; then
		if [ -z "$3" ]; then
			echo "Data file not selected, defaulting to l"
			job1_task1 "l"
		else
			job1_task1 $3
		fi
	elif [ $2 == 2 ]; then
		job1_task2
	elif [ $2 == 3 ]; then
		job1_task3
	fi
elif [ $1 == 2 ]; then
	job2
elif [ $1 == 3 ]; then
	job3 
elif [ $1 == 4 ]; then
	job4
else
	echo "Wrong input!"
fi
