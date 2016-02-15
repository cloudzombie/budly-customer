package com.budly.android.CustomerApp.td.widget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

public class MyRecorder {
	/**
	 * INITIALIZING : recorder is initializing; READY : recorder has been
	 * initialized, recorder not yet started RECORDING : recording ERROR :
	 * reconstruction needed STOPPED: reset needed
	 */
	public class State {
		final static public int INITIALIZING=0x0, READY=0x1, RECORDING=0x2, ERROR=0x3, STOPPED=0x4;
	};

	// The interval in which the recorded samples are output to the file
	// Used only in uncompressed mode
	private static final int TIMER_INTERVAL = 120;

	// Toggles uncompressed recording on/off; RECORDING_UNCOMPRESSED /
	// RECORDING_COMPRESSED

	// Recorder used for compressed recording
	private MediaRecorder mRecorder = null;

	// Stores current amplitude (only in uncompressed mode)
	private int cAmplitude = 0;
	// Output file path
	private String fPath = null;

	// Recorder state; see State
	private int state;

	// File writer (only in uncompressed mode)
	private RandomAccessFile fWriter;

	// Number of channels, sample rate, sample size(size in bits), buffer size,
	// audio source, sample size(see AudioFormat)
	private short nChannels;
	private int sRate;
	private short bSamples;
	private int bufferSize;
	private int aSource;
	private int aFormat;

	// Number of frames written to file on each output(only in uncompressed
	// mode)
	private int framePeriod;

	// Buffer for output(only in uncompressed mode)
	private byte[] buffer;

	// Number of bytes written to file after header(only in uncompressed mode)
	// after stop() is called, this size is written to the header/data chunk in
	// the wave file
	private int payloadSize;

	/**
	 * 
	 * Returns the state of the recorder in a RehearsalAudioRecord.State typed
	 * object. Useful, as no exceptions are thrown.
	 * 
	 * @return recorder state
	 */
	public int getState() {
		return state;
	}
	/**
	 * 
	 * 
	 * Default constructor
	 * 
	 * Instantiates a new recorder, in case of compressed recording the
	 * parameters can be left as 0. In case of errors, no exception is thrown,
	 * but the state is set to ERROR
	 * 
	 */
	public MyRecorder(int audioSource, int sampleRate, int channelConfig, int audioFormat) {
		try {
			// RECORDING_COMPRESSED
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			if (Build.VERSION.SDK_INT > 10) {
				mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			} else {
				mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			}
			cAmplitude = 0;
			fPath = null;
			state = State.INITIALIZING;
		} catch (Exception e) {
			if (e.getMessage() != null) {
				Log.e(MyRecorder.class.getName(), e.getMessage());
			} else {
				Log.e(MyRecorder.class.getName(), "Unknown error occured while initializing recording");
			}
			state = State.ERROR;
		}
	}

	/**
	 * Sets output file path, call directly after construction/reset.
	 * 
	 * @param output
	 *            file path
	 * 
	 */
	public void setOutputFile(String argPath) {
		try {
			if (state == State.INITIALIZING) {
				fPath = argPath;
				mRecorder.setOutputFile(fPath);
			}
		} catch (Exception e) {
			if (e.getMessage() != null) {
				Log.e(MyRecorder.class.getName(), e.getMessage());
			} else {
				Log.e(MyRecorder.class.getName(),
						"Unknown error occured while setting output path");
			}
			state = State.ERROR;
		}
	}

	/**
	 * 
	 * Returns the largest amplitude sampled since the last call to this method.
	 * 
	 * @return returns the largest amplitude since the last call, or 0 when not
	 *         in recording state.
	 * 
	 */
	public int getMaxAmplitude() {
		if (state == State.RECORDING) {
			try {
				return mRecorder.getMaxAmplitude();
			} catch (IllegalStateException e) {
				return 0;
			}
		} else {
			return 0;
		}
	}

	/**
	 * 
	 * Prepares the recorder for recording, in case the recorder is not in the
	 * INITIALIZING state and the file path was not set the recorder is set to
	 * the ERROR state, which makes a reconstruction necessary. In case
	 * uncompressed recording is toggled, the header of the wave file is
	 * written. In case of an exception, the state is changed to ERROR
	 * 
	 */
	public void prepare() {
		try {
			if (state == State.INITIALIZING) {
				mRecorder.prepare();
				state = State.READY;
			} else {
				Log.e(MyRecorder.class.getName(), "prepare() method called on illegal state");
				release();
				state = State.ERROR;
			}
		} catch (Exception e) {
			if (e.getMessage() != null) {
				Log.e(MyRecorder.class.getName(), e.getMessage());
			} else {
				Log.e(MyRecorder.class.getName(),
						"Unknown error occured in prepare()");
			}
			state = State.ERROR;
		}
	}

	/**
	 * 
	 * 
	 * Releases the resources associated with this class, and removes the
	 * unnecessary files, when necessary
	 * 
	 */
	public void release() {
		if (state == State.RECORDING) {
			stop();
		}
		if (mRecorder != null) {
			mRecorder.release();
		}
	}

	/**
	 * 
	 * 
	 * Resets the recorder to the INITIALIZING state, as if it was just created.
	 * In case the class was in RECORDING state, the recording is stopped. In
	 * case of exceptions the class is set to the ERROR state.
	 * 
	 */
	public void reset() {
		try {
			if (state != State.ERROR) {
				release();
				fPath = null; // Reset file path
				cAmplitude = 0; // Reset amplitude

				mRecorder = new MediaRecorder();
				mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				if (Build.VERSION.SDK_INT > 10) {
					mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				} else {
					mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				}
				state = State.INITIALIZING;
			}
		} catch (Exception e) {
			Log.e(MyRecorder.class.getName(), e.getMessage());
			state = State.ERROR;
		}
	}

	/**
	 * 
	 * 
	 * Starts the recording, and sets the state to RECORDING. Call after
	 * prepare().
	 * 
	 */
	public void start() {
		if (state == State.READY) {

			mRecorder.start();

			state = State.RECORDING;
		} else {
			Log.e(MyRecorder.class.getName(),
					"start() called on illegal state");
			state = State.ERROR;
		}
	}

	/**
	 * 
	 * 
	 * Stops the recording, and sets the state to STOPPED. In case of further
	 * usage, a reset is needed. Also finalizes the wave file in case of
	 * uncompressed recording.
	 * 
	 */
	public void stop() {
		if (state == State.RECORDING) {
			mRecorder.stop();
			state = State.STOPPED;
		} else {
			Log.e(MyRecorder.class.getName(), "stop() called on illegal state");
			state = State.ERROR;
		}
	}

	/*
	 * 
	 * Converts a byte[2] to a short, in LITTLE_ENDIAN format
	 */
	private short getShort(byte argB1, byte argB2) {
		return (short) (argB1 | (argB2 << 8));
	}
	
	public Boolean isRecording() {
		return (getState()!=State.STOPPED && getState()!=State.ERROR);
	}
	
	public static void copy(File src, File dst) {
		try {
		    InputStream in = new FileInputStream(src);
		    OutputStream out = new FileOutputStream(dst);
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		    in.close();
		    out.close();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
