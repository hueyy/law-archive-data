CREATE VIEW hearing_type_count AS SELECT COUNT(*) AS _count, type from item_version_detail GROUP BY type ORDER BY _count DESC;
CREATE VIEW coram_count AS SELECT COUNT(*) AS _count, coram from item_version_detail GROUP BY coram ORDER BY _count DESC;
CREATE VIEW venue_count AS SELECT COUNT(*) AS _count, venue from item_version_detail GROUP BY venue ORDER BY _count DESC;
CREATE VIEW nature_count AS SELECT COUNT(*) AS _count, `nature-of-case` from item_version_detail GROUP BY `nature-of-case` ORDER BY _count DESC;