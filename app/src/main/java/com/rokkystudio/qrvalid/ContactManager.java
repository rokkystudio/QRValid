package com.rokkystudio.qrvalid;

import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

import java.util.ArrayList;

import ezvcard.VCard;

public class ContactManager
{
    private void addVCard(Context context, VCard vCard)
    {
        vCard.getTelephoneNumbers()

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        int rawContactInsertIndex = 0;

        operations.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
            .withValue(RawContacts.ACCOUNT_TYPE, AccountManager.KEY_ACCOUNT_TYPE)
            .withValue(RawContacts.ACCOUNT_NAME, AccountManager.KEY_ACCOUNT_NAME)
            .build());

        if (vCard.getFormattedName() != null) {
            operations.add(opDisplayName(vCard.getFormattedName().getValue(), rawContactInsertIndex));
        }

        if (vCard.getStructuredName() != null) {
            operations.add(opFamilyName(vCard.getStructuredName().getFamily(), rawContactInsertIndex));
            operations.add(opGivenName(vCard.getStructuredName().getGiven(), rawContactInsertIndex));
        }

        operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,"23232343434")
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, "4343")
                .build());

        operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, "")
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, "")
                .build());

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private ContentProviderOperation opDisplayName(String name, int index) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, index)
            .withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(StructuredName.DISPLAY_NAME, name)
            .withValue(StructuredName.IN_VISIBLE_GROUP, true)
            .build();
    }

    private ContentProviderOperation opFamilyName(String name, int index) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, index)
            .withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(StructuredName.FAMILY_NAME, name)
            .withValue(StructuredName.IN_VISIBLE_GROUP, true)
            .build();
    }

    private ContentProviderOperation opGivenName(String name, int index) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, index)
            .withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(StructuredName.GIVEN_NAME, name)
            .withValue(StructuredName.IN_VISIBLE_GROUP, true)
            .build();
    }


    public void insert() {
        Intent intent = new Intent(
            ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
            ContactsContract.Contacts.CONTENT_URI);
        intent.setData(Uri.parse("tel:911"));//specify your number here
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, "Emergency USA");
        startActivity(intent);
        Toast.makeText(this, "Record inserted", Toast.LENGTH_SHORT).show();
    }
}
