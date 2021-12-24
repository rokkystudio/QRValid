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
import android.provider.ContactsContract.CommonDataKinds.Phone;

import java.util.ArrayList;
import java.util.List;

import ezvcard.VCard;
import ezvcard.property.Email;
import ezvcard.property.Telephone;

public class ContactManager
{
    private void addVCard(Context context, VCard vCard)
    {
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

        if (vCard.getTelephoneNumbers() != null) {
            for (Telephone telephone : vCard.getTelephoneNumbers()) {
                if (telephone != null) {
                    operations.add(opPhoneNumber(telephone.getText(), "TYPE", rawContactInsertIndex));
                }
            }
        }

        if (vCard.getEmails() != null) {
            for (Email email : vCard.getEmails()) {
                if (email != null) {
                    email.
                }
            }
        }
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

    private ContentProviderOperation opPhoneNumber(String number, String type, int index) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, index)
            .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
            .withValue(Phone.NUMBER, number)
            .withValue(Phone.TYPE, type)
            .build();
    }
}
