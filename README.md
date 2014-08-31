ExoPlayerCompat
===============

ExoPlayer Compatibility Library:when ExoPlayer is invalid ,it use MediaPlayer instead of ExoPlayer

Why
===
1. Use a universe interface to control mediaplay.Make it easier to change MediaPlayer's implement.

2. Provide a backup solution that use raw MediaPlayer API repleace ExoPlayer when exoplayer is not valid in your rom.

3. To avoid some bugs we found which not fixed in official project.

ToDo List
=========
1. ~~add flag to judge if MediaPlayer should be switched t2. o Raw MediaPlayer when  ExoPlayer ‘prepare’ failed. ~~
2. To extract the universe interface to a  new dependent library.
2. ExoPlayer friendly white-list. 
1. Add 'setSpeed' method