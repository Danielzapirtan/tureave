#! /bin/bash

years="2026"
months="9 10"
year=2026
month=10
shiftDays=""
for n in $(seq 1 4 31); do
  n2=$(($n + 1))
  shiftDays="$shiftDays $n $n2"
done
leaveDays=""

showStats() {
  numShift=$(echo $shiftDays|wc -w)
  numLeave=$(echo $leaveDays|wc -w)
  numHours=$(($numShift * 12 + $numLeave * 8))
  echo "$numLeave leave days"
  echo "$numHours worked hours"
}

toggle() {
  ARG=$1
  if echo $shiftDays|grep -qw $ARG; then
    echo $shiftDays|sed -e "s|\<$ARG\>||g" | fmt | tr -s " ">/tmp/tmp
    shiftDays="$(cat /tmp/tmp)"
    leaveDays="$(echo $leaveDays|cat; echo $ARG)"
    for d in $leaveDays; do
      echo $d;
    done |sort -n >/tmp/tmp
    leaveDays="$(cat /tmp/tmp|fmt)"
  elif echo $leaveDays|grep -qw $ARG; then
    echo $leaveDays|sed -e "s|\<$ARG\>||g" | fmt | tr -s " ">/tmp/tmp
    leaveDays="$(cat /tmp/tmp)"
    shiftDays="$(echo $shiftDays|cat; echo $ARG)"
    for d in $shiftDays; do
      echo $d;
    done |sort -n >/tmp/tmp
    shiftDays="$(cat /tmp/tmp|fmt)"
  fi
}

interactive() {
  echo "shift days $shiftDays"
  echo "leave days $leaveDays"
  showStats
  echo -n "Toggle what day? "
  read toggleDay
  toggle $toggleDay
}

while true; do
  interactive
done

