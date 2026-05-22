package com.pokiepaws.api.unit;

import static org.mockito.Mockito.verify;

import com.pokiepaws.api.models.Prescription;
import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.services.MobilePushNotificationService;
import com.pokiepaws.api.services.OwnerEmailNotificationService;
import com.pokiepaws.api.services.OwnerNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OwnerNotificationServiceTest {

  @Mock MobilePushNotificationService mobilePushNotificationService;
  @Mock OwnerEmailNotificationService ownerEmailNotificationService;

  private OwnerNotificationService service;

  @BeforeEach
  void setUp() {
    service =
        new OwnerNotificationService(mobilePushNotificationService, ownerEmailNotificationService);
  }

  @Test
  void prescriptionCreated_shouldPassPrescriptionToMobilePushSoPayloadCanIncludePrescriptionId() {
    Visit visit = Visit.builder().id(50L).build();
    Prescription prescription = Prescription.builder().id(90L).visit(visit).build();

    service.prescriptionCreated(prescription);

    verify(mobilePushNotificationService).sendPrescriptionCreated(prescription);
    verify(ownerEmailNotificationService).sendPrescriptionCreated(visit);
  }
}
