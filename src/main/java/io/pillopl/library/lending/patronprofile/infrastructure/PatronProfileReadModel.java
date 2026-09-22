package io.pillopl.library.lending.patronprofile.infrastructure;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.PatronStatus;
import io.pillopl.library.lending.patronprofile.model.Checkout;
import io.pillopl.library.lending.patronprofile.model.CheckoutsView;
import io.pillopl.library.lending.patronprofile.model.Hold;
import io.pillopl.library.lending.patronprofile.model.HoldsView;
import io.pillopl.library.lending.patronprofile.model.PatronProfile;
import io.pillopl.library.lending.patronprofile.model.PatronProfiles;

class PatronProfileReadModel implements PatronProfiles {

  private final JdbcTemplate sheets;

  PatronProfileReadModel(JdbcTemplate sheets) {
    this.sheets = sheets;
  }

  @Override
  public PatronProfile fetchFor(PatronId patronId) {
    HoldsView holdsView =
        new HoldsView(findCurrentHoldsFor(patronId).stream().map(this::toHold).toList());
    CheckoutsView checkoutsView =
        new CheckoutsView(
            findCurrentCheckoutsFor(patronId).stream().map(this::toCheckout).toList());
    return new PatronProfile(findStatusOf(patronId), holdsView, checkoutsView);
  }

  private PatronStatus findStatusOf(PatronId patronId) {
    return sheets.queryForObject(
        "SELECT status FROM patron_database_entity WHERE patron_id = ?",
        (rs, rowNum) -> PatronStatus.valueOf(rs.getString("status")),
        patronId.getPatronId());
  }

  private List<Map<String, Object>> findCurrentHoldsFor(PatronId patronId) {
    return sheets.query(
        "SELECT h.book_id, h.hold_till FROM holds_sheet h WHERE h.hold_by_patron_id = ? AND h.checked_out_at IS NULL AND h.expired_at IS NULL AND h.canceled_at IS NULL",
        new ColumnMapRowMapper(),
        patronId.getPatronId());
  }

  private Hold toHold(Map<String, Object> map) {
    Timestamp holdTill = (Timestamp) map.get("HOLD_TILL");
    return new Hold(
        new BookId((UUID) map.get("BOOK_ID")), holdTill == null ? null : holdTill.toInstant());
  }

  private List<Map<String, Object>> findCurrentCheckoutsFor(PatronId patronId) {
    return sheets.query(
        "SELECT h.book_id, h.checkout_till FROM checkouts_sheet h WHERE h.checked_out_by_patron_id = ? AND h.returned_at IS NULL",
        new ColumnMapRowMapper(),
        patronId.getPatronId());
  }

  private Checkout toCheckout(Map<String, Object> map) {
    return new Checkout(
        new BookId((UUID) map.get("BOOK_ID")), ((Timestamp) map.get("CHECKOUT_TILL")).toInstant());
  }
}
