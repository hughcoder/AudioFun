package com.hugh.libwebrtc.commonaudio;

public class BufferSlice {
    private short[] _old = null;
    private int _old_offset = 0;
    private int _slice_size ;


    public static interface ISliceOutput {
        public void onOutput(short[] slice, int stamp);
    }
    public BufferSlice(int slice_size){
        _slice_size = slice_size;
        _old = new short[_slice_size];
    }

    public void input(short[] data,int len,int stamp,int sample_ms, ISliceOutput callback){
        int remain = len;
        while (remain > 0){
            int size = Math.min(remain - _old_offset,_slice_size - _old_offset);
            int offset = len - remain;
            int stamp_slice = stamp + ((offset - _old_offset) * sample_ms / len);
            if(offset + size > data.length){
                return;
            }
            System.arraycopy(data ,offset,_old,_old_offset,size);
            _old_offset += size;
            remain -= size;

            if(_old_offset == _slice_size){
                //包满了,消费
                callback.onOutput(_old,stamp_slice);
                _old_offset = 0;
            }
        }
    }
    public void clear(){
        _old_offset = 0;
    }
}
