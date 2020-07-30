# AudioFun

音频操作的尝试  fmod  soundTouch webRtc


## SoundTouch

### 相关参数概念

-tempo = n
将声音速度更改n个百分点（n = -95.0 .. +5000.0％）
-pitch = n
改变音调n个半音（n = -60.0 .. + 60.0半音）
-rate = n
将声音播放率更改为n个百分点（n = -95.0 .. +5000.0％）
-bpm = n
检测声音的每分钟节拍（BPM）速率，并调整速度以满足“ n”个BPM。当应用此开关时，将忽略“ -tempo”开关。如果省略“ = n”，即单独使用开关“ -bpm”，则将估算并显示BPM速率，但速度不会根据BPM值进行调整。

## FMOD

FMOD 修改音频参数

## webRtc

移植webRtc模块