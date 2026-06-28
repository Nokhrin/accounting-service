#!/bin/bash
# example:
# ./calc-total-time.sh "14:00-15:20" "15:50-16:30" "17:00-18:40"

total_minutes=0

for interval in "$@"; do
    start=$(echo "$interval" | cut -d'-' -f1)
    end=$(echo "$interval" | cut -d'-' -f2)

    start_h=$(echo "$start" | cut -d':' -f1)
    start_m=$(echo "$start" | cut -d':' -f2)
    end_h=$(echo "$end" | cut -d':' -f1)
    end_m=$(echo "$end" | cut -d':' -f2)

    start_total=$((start_h * 60 + start_m))
    end_total=$((end_h * 60 + end_m))

    diff=$((end_total - start_total))
    total_minutes=$((total_minutes + diff))
done

hours=$(echo "scale=2; $total_minutes / 60" | bc)
echo "Minutes: $total_minutes == Hours: $hours"