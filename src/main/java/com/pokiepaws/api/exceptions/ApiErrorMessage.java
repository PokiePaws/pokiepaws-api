package com.pokiepaws.api.exceptions;

public final class ApiErrorMessage {

  public static final String ACCESS_DENIED = "Access denied";
  public static final String ANIMAL_NOT_OWNED = "You are not the owner of this animal";
  public static final String CANCEL_FORBIDDEN = "You cannot cancel this visit";
  public static final String CANCELLED_VISIT_CONFIRM_FORBIDDEN = "Cannot confirm cancelled visit";
  public static final String CANCELLED_VISIT_UPDATE_FORBIDDEN = "Cannot update cancelled visit";
  public static final String CLINIC_NOT_FOUND = "Clinic not found";
  public static final String DATE_RANGE_INVALID = "'from' must be <= 'to'";
  public static final String ONLY_SCHEDULED_VISIT_CAN_BE_CONFIRMED =
      "Only SCHEDULED visit can be confirmed";
  public static final String OWNER_PROFILE_NOT_FOUND = "Owner profile not found";
  public static final String PRESCRIPTION_ALREADY_EXISTS =
      "Prescription already exists for this visit";
  public static final String PRESCRIPTION_NOT_FOUND = "Prescription not found for this visit";
  public static final String PRODUCT_NOT_AVAILABLE_IN_CLINIC_STOCK =
      "Product not available in clinic stock: ";
  public static final String PRODUCT_NOT_FOUND = "Product not found: ";
  public static final String SELECTED_SLOT_ALREADY_TAKEN = "Selected slot is already taken";
  public static final String SELECTED_TIME_OUTSIDE_WORKING_HOURS =
      "Selected time is outside working hours";
  public static final String SELECTED_VET_DOES_NOT_BELONG_TO_CLINIC =
      "Selected vet does not belong to selected clinic";
  public static final String SLOT_ALIGNMENT_INVALID = "Start time must align to visit slot length";
  public static final String USER_NOT_FOUND = "User not found";
  public static final String VET_NOT_ASSIGNED_TO_VISIT = "You are not the vet for this visit";
  public static final String VET_NOT_FOUND = "Vet not found";
  public static final String VISIT_HAS_NO_ASSIGNED_VET = "Visit has no assigned vet";
  public static final String VISIT_NOT_FOUND = "Visit not found";
  public static final String VISIT_NOT_ASSIGNED_TO_CURRENT_VET =
      "You are not assigned vet for this visit";
  public static final String VISIT_NOT_OWNED = "This visit is not yours";

  private ApiErrorMessage() {}
}
