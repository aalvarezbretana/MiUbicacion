package com.example.miubicacion

import android.Manifest
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.OperationApplicationException

import android.provider.ContactsContract

import android.content.ContentResolver

import android.content.ContentProviderOperation
import android.content.pm.PackageManager
import android.widget.Toast
import android.content.DialogInterface
import android.os.RemoteException
import android.util.Log


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private val TAG = "Contacts"
    private fun insertDummyContact() {
        // Two operations are needed to insert a new contact.
        val operations = ArrayList<ContentProviderOperation>(2)

        // First, set up a new raw contact.
        var op = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
        operations.add(op.build())

        // Next, set the name for the contact.
        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            .withValue(
                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                "__DUMMY CONTACT from runtime permissions sample"
            )
        operations.add(op.build())

        // Apply the operations.
        val resolver = contentResolver
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, operations)
        } catch (e: RemoteException) {
            Log.d(TAG, "Could not add a new contact: " + e.message)
        } catch (e: OperationApplicationException) {
            Log.d(TAG, "Could not add a new contact: " + e.message)
        }
    }

    private val REQUEST_CODE_ASK_PERMISSIONS = 123

    private fun insertDummyContactWrapper() {
        val hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_CONTACTS)
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                showMessageOKCancel(
                    "You need to allow access to Contacts"
                ) { dialog, which ->
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_CONTACTS),
                        REQUEST_CODE_ASK_PERMISSIONS
                    )
                }
                return
            }
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_CONTACTS),
                REQUEST_CODE_ASK_PERMISSIONS
            )
            return
        }
        insertDummyContact()
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                insertDummyContact()
            } else {
                // Permission Denied
                Toast.makeText(this@MainActivity, "WRITE_CONTACTS Denied", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }
    }
}

