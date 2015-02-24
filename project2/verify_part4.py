import os

mv_files = os.listdir('subset-1percent')
print len(mv_files)
for mv_file in mv_files:
  mv_id = None
  five_count = 0
  one_count = 0
  for line in open('subset-1percent/' + mv_file):
    if (mv_id == None):
      mv_id = line
    else:
      splitted = line.split(",")
      rating = int(splitted[1])
      if (rating == 5):
        five_count += 1
      elif (rating == 1):
        one_count += 1

  print mv_file + " : " + str(one_count) + " : " + str(five_count)