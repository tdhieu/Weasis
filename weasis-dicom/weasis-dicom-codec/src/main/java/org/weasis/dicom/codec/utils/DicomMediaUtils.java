/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/

package org.weasis.dicom.codec.utils;

import java.awt.image.ByteLookupTable;
import java.awt.image.LookupTable;
import java.awt.image.ShortLookupTable;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Benoit Jacquemoud
 * @version $Rev$ $Date$
 */
public class DicomMediaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DicomMediaUtils.class);

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @return false if either an argument is null or if at least one tag value is empty in the given tagMap
     */
    // public static boolean containsRequiredTags(Map<TagW, Object> tagMap, TagW... requiredTags) {
    // if (tagMap == null || requiredTags == null || requiredTags.length == 0)
    // return false;
    //
    // int countValues = 0;
    // List<String> missingTagList = null;
    //
    // for (TagW tag : requiredTags) {
    // Object value = tagMap.get(tag);
    // if (value != null) {
    // countValues++;
    // } else {
    // if (missingTagList == null) {
    // missingTagList = new ArrayList<String>(requiredTags.length);
    // }
    // missingTagList.add(tag.toString());
    // }
    // }
    // if (countValues > 0 && countValues < requiredTags.length) {
    // LOGGER.debug("Missing Tags \"{}\" in required list \"{}\"", missingTagList, requiredTags);
    // }
    // return (countValues != requiredTags.length);
    // }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @return false if either an argument is null or if at least one tag value is empty in the given dicomObject
     */
    public static boolean containsRequiredAttributes(DicomObject dicomObj, int... requiredTags) {
        if (dicomObj == null || requiredTags == null || requiredTags.length == 0)
            return false;

        int countValues = 0;
        List<String> missingTagList = null;

        for (int tag : requiredTags) {
            DicomElement attr = dicomObj.get(tag);
            if (attr != null && !attr.isEmpty()) {
                countValues++;
            } else {
                if (missingTagList == null) {
                    missingTagList = new ArrayList<String>(requiredTags.length);
                }
                missingTagList.add(TagUtils.toString(tag));
            }
        }
        if (countValues > 0 && countValues < requiredTags.length) {
            LOGGER.debug("Missing attributes \"{}\" in required list \"{}\"", missingTagList, requiredTags);
        }
        return (countValues == requiredTags.length);
    }

    /**
     * @return False if either an argument is null or if at least one tag value is empty in the first nested sequence of
     *         the given DicomObject for the given sequence Tag
     */
    public static boolean containsRequiredAttributes(DicomObject dicomObj, int sequenceTag, int... requiredTags) {
        DicomElement sequenceElt = (dicomObj != null) ? dicomObj.get(sequenceTag) : null;
        return containsRequiredAttributes(sequenceElt, 0, requiredTags);
    }

    /**
     * @return False if either an argument is null or if at least one tag value is empty in the given dicomElement
     *         Sequence at the given index
     */
    public static boolean containsRequiredAttributes(DicomElement sequenceElt, int itemIndex, int... requiredTags) {
        if (sequenceElt == null || sequenceElt.isEmpty()) {
        } else if (sequenceElt.vr() != VR.SQ) {
            LOGGER.debug("Invalid DicomElement argument \"{}\" which is not a sequence", sequenceElt.toString());
        } else if (sequenceElt.countItems() <= itemIndex) {
            LOGGER.debug("Index \"{}\" is out of bound for this sequence \"{}\"", itemIndex, sequenceElt.toString());
        } else
            return containsRequiredAttributes(sequenceElt.getDicomObject(itemIndex), requiredTags);

        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static final int[] ModalityLUTRescaleAttributes = //
        new int[] { Tag.RescaleIntercept, Tag.RescaleSlope, Tag.RescaleType };

    public static boolean containsRequiredModalityLUTRescaleAttributes(DicomObject dicomObj) {
        return containsRequiredAttributes(dicomObj, ModalityLUTRescaleAttributes);
    }

    public static final int[] ModalityLUTSequenceAttributes = //
        new int[] { Tag.ModalityLUTType, Tag.LUTDescriptor, Tag.LUTData };

    public static boolean containsRequiredModalityLUTSequence(DicomObject dicomObj) {
        return containsRequiredAttributes(dicomObj, Tag.ModalityLUTSequence, ModalityLUTSequenceAttributes);
    }

    public static boolean containsRequiredModalityLUTSequenceAttributes(DicomElement sequenceElt) {
        // Only a single Item shall be included in this sequence
        return containsRequiredAttributes(sequenceElt, 0, ModalityLUTSequenceAttributes);
    }

    /**
     * Either a Modality LUT Sequence containing a single Item or Rescale Slope and Intercept values shall be present
     * but not both.<br>
     * This requirement for only a single transformation makes it possible to unambiguously define the input of
     * succeeding stages of the grayscale pipeline such as the VOI LUT
     * 
     * @return True if the specified object contains some type of Modality LUT attributes at the current level. <br>
     * 
     * @see - Dicom Standard 2011 - PS 3.3 § C.11.1 Modality LUT Module
     */

    public static boolean containsRequiredModalityLUTAttributes(DicomObject dicomObj) {
        return containsRequiredModalityLUTRescaleAttributes(dicomObj) || //
            containsRequiredModalityLUTSequence(dicomObj);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final int[] VOILUTWindowLevelAttributes = //
        new int[] { Tag.WindowCenter, Tag.WindowWidth };

    public static boolean containsRequiredVOILUTWindowLevelAttributes(DicomObject dicomObj) {
        return containsRequiredAttributes(dicomObj, VOILUTWindowLevelAttributes);
    }

    public static final int[] VOILUTSequenceAttributes = //
        new int[] { Tag.LUTDescriptor, Tag.LUTData };

    public static boolean containsRequiredVOILUTSequence(DicomObject dicomObj) {
        return containsRequiredAttributes(dicomObj, Tag.VOILUTSequence, VOILUTSequenceAttributes);
    }

    public static boolean containsRequiredVOILUTSequenceAttributes(DicomElement sequenceElt) {
        // One or more Items shall be included in this sequence, only first is considered
        return containsRequiredAttributes(sequenceElt, 0, VOILUTSequenceAttributes);
    }

    /**
     * 
     * If any VOI LUT Table is included by an Image, a Window Width and Window Center or the VOI LUT Table, but not
     * both, may be applied to the Image for display. Inclusion of both indicates that multiple alternative views may be
     * presented. <br>
     * If multiple items are present in VOI LUT Sequence, only one may be applied to the Image for display. Multiple
     * items indicate that multiple alternative views may be presented.
     * 
     * @return True if the specified object contains some type of VOI LUT attributes at the current level (ie:Window
     *         Level or VOI LUT Sequence).
     * 
     * @see - Dicom Standard 2011 - PS 3.3 § C.11.2 VOI LUT Module
     */
    public static boolean containsRequiredVOILUTAttributes(DicomObject dicomObj) {
        boolean windowLevelAttributes = containsRequiredVOILUTWindowLevelAttributes(dicomObj);
        boolean sequenceAttributes = containsRequiredVOILUTSequence(dicomObj);

        return windowLevelAttributes || sequenceAttributes;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 
     * @param dicomLutObject
     *            defines LUT data dicom structure
     * @return LookupTable object if Data Element and Descriptors are consistent
     * 
     * @see - Dicom Standard 2011 - PS 3.3 § C.11 LOOK UP TABLES AND PRESENTATION STATES
     */
    public static LookupTable createLut(DicomObject dicomLutObject) {
        if (dicomLutObject == null || dicomLutObject.isEmpty())
            return null;

        LookupTable lookupTable = null;

        // Three values of the LUT Descriptor describe the format of the LUT Data in the corresponding Data Element
        int[] descriptor = dicomLutObject.getInts(Tag.LUTDescriptor);

        if (descriptor == null) {
            LOGGER.debug("Missing LUT Descriptor");
        } else if (descriptor.length != 3) {
            LOGGER.debug("Illegal number of LUT Descriptor values \"{}\"", descriptor.length);
        } else {

            // First value is the number of entries in the lookup table.
            // When this value is 0 the number of table entries is equal to 0x10000 (<=> 65536) .
            int numEntries = (descriptor[0] == 0) ? 0x10000 : descriptor[0];
            // Second value is the first input value mapped.
            int offset = descriptor[1];
            // Third value specifies the number of bits for each entry in the LUT Data.
            int numBits = descriptor[2];

            int dataLength = 0; // number of entry values in the LUT Data.

            if (numBits <= 8) { // LUT entry value range should be [0,255]
                byte[] bData = dicomLutObject.getBytes(Tag.LUTData); // LUT Data contains the LUT entry values.

                if (bData.length == (numEntries << 1)) {
                    // Appends when some implementations have encoded 8 bit entries with 16 bits
                    // allocated, padding the high bits;
                    byte[] bDataNew = new byte[numEntries];
                    int byteShift = (dicomLutObject.bigEndian() ? 1 : 0);
                    for (int i = 0; i < numEntries; i++) {
                        bDataNew[i] = bData[i << 2 + byteShift];
                    }
                    bData = bDataNew;
                }
                dataLength = bData.length;
                lookupTable = new ByteLookupTable(offset, bData);

            } else if (numBits <= 16) { // LUT entry value range should be [0,65535]
                short[] sData = dicomLutObject.getShorts(Tag.LUTData); // LUT Data contains the LUT entry values.

                dataLength = sData.length;
                lookupTable = new ShortLookupTable(offset, sData);
            } else {
                LOGGER.debug("Illegal number of bits for each entry in the LUT Data");
            }

            if (lookupTable != null) {
                if (dataLength != numEntries) {
                    LOGGER.debug("LUT Data length \"{}\" mismatch number of entries \"{}\" in LUT Descriptor ",
                        dataLength, numEntries);
                }
                if (dataLength > (1 << numBits)) {
                    LOGGER.debug(
                        "Illegal LUT Data length \"{}\" with respect to the number of bits in LUT descriptor \"{}\"",
                        dataLength, numBits);
                    // lookupTable = null;
                }
            }
        }
        return lookupTable;
    }
}
