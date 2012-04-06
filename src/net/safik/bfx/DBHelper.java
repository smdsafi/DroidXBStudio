package net.safik.bfx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DBHelper extends SQLiteOpenHelper {

	private SQLiteDatabase db;
	private static final int DATABASE_VERSION = 1;
	private static final String DB_NAME = "bfx_design.db";
	private static final String TABLE_NAME = "bfx_master";
	private Context context = null;
	private File file = null;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the application context
	 */
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		db = getWritableDatabase();
		this.context = context;

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			file = Environment.getExternalStorageDirectory();
			file = new File(file, "XtremeBusinesss");
			if (!file.exists()) {
				boolean boo=file.mkdirs();
			}
		}

	}

	/**
	 * Called at the time to create the DB. The create DB statement
	 * 
	 * @param the
	 *            SQLite DB
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + TABLE_NAME
				+ " (_id integer primary key autoincrement,id text not null, "
				+ "xml blob not null,process text," + "type text not null,"
				+ "status integer not null)");
	}

	/**
	 * The Insert DB statement
	 * 
	 * @param id
	 *            the friends id to insert
	 * @param name
	 *            the friend's name to insert
	 */
	public void insert(BfxElement b) {

		ContentValues dataToInsert = new ContentValues();
		dataToInsert.put("id", b.getId());
		dataToInsert.put("xml", b.toXml().getBytes());
		if (b.getProcess() != null)
			dataToInsert.put("process", b.getProcess().getId());
		dataToInsert.put("type", b.getType().name());
		dataToInsert.put("status", 1);

		db.insert(TABLE_NAME, null, dataToInsert);
		db.close();
		writeFile(b);
	}

	public void writeFile(BfxElement b) {

		String xml = b.toXml();
		String filename = b.getId().toLowerCase() + "."
				+ b.getType().name().toLowerCase() + ".xml";

		File f = new File(file, filename.toLowerCase());
		if (b.getProcess() != null) {

			File pf = new File(file, b.getProcess().getId().toLowerCase());
			if (!pf.exists())
				pf.mkdir();
			f = new File(pf, filename.toLowerCase());
		}
		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(f));

			writer.write(xml);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * The Update DB statement
	 * 
	 * @param id
	 *            the friends id to insert
	 * @param name
	 *            the friend's name to insert
	 */
	public void update(BfxElement b) {

		db.beginTransaction();
		ContentValues dataToInsert = new ContentValues();
		dataToInsert.put("id", b.getId());

		db.update(TABLE_NAME, dataToInsert, "id='" + b.getId() + "'", null);
		db.endTransaction();
		db.close();

		writeFile(b);
	}

	/**
	 * update status
	 * 
	 * @param bill_name
	 * @param status
	 */
	public void delete(BfxElement b) {
		db.execSQL("DELETE  " + TABLE_NAME + " WHERE _id=" + b.getId());
		db.close();

		String filename = b.getId() + "." + b.getType().name().toLowerCase()
				+ ".xml";
		if (b.getProcess() != null)
			filename = b.getProcess() + File.separator + filename;
		this.context.deleteFile(filename);
	}

	/**
	 * Select All returns a cursor
	 * 
	 * @return the cursor for the DB selection
	 */
	public Cursor cursorSelectAll() {
		Cursor cursor = this.db.query(TABLE_NAME, new String[] { "_id", "id",
				"xml", "type", "process", "status" }, // Columns to return
				null, // SQL WHERE
				null, // Selection Args
				null, // SQL GROUP BY
				null, // SQL HAVING
				"id"); // SQL ORDER BY

		return cursor;
	}

	/**
	 * Select All returns a cursor
	 * 
	 * @return the cursor for the DB selection
	 */
	public BfxElement cursorSelectElement(String id, String type) {
		Cursor cursor = this.db.query(TABLE_NAME, new String[] { "_id", "id",
				"xml", "type", "process", "status" }, // Columns to return
				"id='" + id + "' AND type='" + type + "'", // SQL WHERE
				null, // Selection Args
				null, // SQL GROUP BY
				null, // SQL HAVING
				"id"); // SQL ORDER BY
		cursor.moveToFirst();
		BfxElement b = null;
		if (cursor.getCount() > 0) {
			b = BfxFactory.createElement(id, BfxType.valueOf(type.toUpperCase()));
			b.setNo(cursor.getInt(0));
			b.setId(cursor.getString(1));

			b.setType(BfxType.valueOf(cursor.getString(3)));
			String process = cursor.getString(4);
			if (process != null) {
				BfxElement p = BfxFactory.createElement(process, BfxType.PROCESS);
				
				b.setProcess(p);
			}

		}
		cursor.close();

		return b;
	}

	/**
	 * Select All returns a cursor
	 * 
	 * @return the cursor for the DB selection
	 */
	public String cursorSelectXml(BfxElement b) {
		Cursor cursor = this.db.query(TABLE_NAME, new String[] { "_id", "id",
				"xml", "type", "process", "status" }, // Columns to return
				"id='" + b.getId() + "' AND type='" + b.getType() + "'", // SQL
																			// WHERE
				null, // Selection Args
				null, // SQL GROUP BY
				null, // SQL HAVING
				"id"); // SQL ORDER BY
		cursor.moveToFirst();

		String process = null;
		if (cursor.getCount() > 0) {
			
			b.setNo(cursor.getInt(0));
			b.setId(cursor.getString(1));
			b.setType(BfxType.valueOf(cursor.getString(3)));
			process = cursor.getString(4);
			if (process != null) {
				b.setProcess(BfxFactory.configureElement(this, process,
						BfxType.PROCESS.name()));
			}
		}

		cursor.close();

		return readFile(b);
	}

	public String readFile(BfxElement b) {
		String xml = "";
		String filename = b.getId() + "." + b.getType().name().toLowerCase()
				+ ".xml";
		File f = new File(file, filename.toLowerCase());
		if (b.getProcess() != null) {

			f = new File(new File(file, b.getProcess().getId().toLowerCase()),
					filename.toLowerCase());

		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			xml = builder.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xml;
	}

	public String dateToString(Date date) {
		SimpleDateFormat d = new SimpleDateFormat("dd/mm/yyyy");
		return d.format(date);
	}

	public long timeToLong(java.sql.Time date) {
		return date.getTime();
	}

	public Time longToTime(long date) {

		Time t = new Time(date);

		return t;
	}

	public Date stringToDate(String date) {
		SimpleDateFormat d = new SimpleDateFormat("dd/mm/yyyy");
		Date dt = null;
		try {
			dt = d.parse(date);
		} catch (ParseException e) {

		}
		return new java.sql.Date(dt.getTime());
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

	}

}
