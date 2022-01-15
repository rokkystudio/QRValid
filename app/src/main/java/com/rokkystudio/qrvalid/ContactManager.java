package com.rokkystudio.qrvalid;

import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds;

import java.util.ArrayList;

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
                    operations.add(opPhoneNumber(telephone.getText(), rawContactInsertIndex));
                }
            }
        }

        if (vCard.getEmails() != null) {
            for (Email email : vCard.getEmails()) {
                if (email != null) {
                    operations.add(opEmail(email.getValue(), rawContactInsertIndex));
                }
            }
        }

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
            .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, name)
            .withValue(CommonDataKinds.StructuredName.IN_VISIBLE_GROUP, true)
            .build();
    }

    private ContentProviderOperation opFamilyName(String name, int index) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, index)
            .withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(CommonDataKinds.StructuredName.FAMILY_NAME, name)
            .withValue(CommonDataKinds.StructuredName.IN_VISIBLE_GROUP, true)
            .build();
    }

    private ContentProviderOperation opGivenName(String name, int index) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, index)
            .withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(CommonDataKinds.StructuredName.GIVEN_NAME, name)
            .withValue(CommonDataKinds.StructuredName.IN_VISIBLE_GROUP, true)
            .build();
    }

    private ContentProviderOperation opPhoneNumber(String number, int index) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, index)
            .withValue(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(CommonDataKinds.Phone.NUMBER, number)
            .withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
            .build();
    }

    private ContentProviderOperation opEmail(String email, int index) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, index)
            .withValue(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(CommonDataKinds.Email.ADDRESS, email)
            .withValue(CommonDataKinds.Email.TYPE, CommonDataKinds.Email.TYPE_HOME)
            .build();
    }
}
