package com.example.springboottemplate.utils;

import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.mapper.StoreMapper;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 商户租期：storeType=1 永久，2 租用。到期日当天仍有效，次日零点起视为到期。
 * 到期只拦截展示与登录，不删除商品/业务数据。
 */
public final class StoreExpireHelper {
    public static final int TYPE_FOREVER = 1;
    public static final int TYPE_RENT = 2;
    public static final String EXPIRED_LOGIN_MSG = "店铺租期已到期，无法登录后台";
    public static final String EXPIRED_BROWSE_MSG = "店铺租期已到期";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATETIME_MS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    private static final DateTimeFormatter CTIME_FMT =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
    private static final Pattern ISO_DATE = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern SLASH_DATE = Pattern.compile("(\\d{4})/(\\d{2})/(\\d{2})");

    private StoreExpireHelper() {
    }

    public static boolean isRentType(Integer storeType) {
        return storeType != null && storeType == TYPE_RENT;
    }

    public static boolean isExpired(Store store) {
        if (store == null || !isRentType(store.getStoreType())) {
            return false;
        }
        LocalDate expireDate = parseExpireDate(store.getStoreTime());
        if (expireDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now(StoreOpenHelper.SHANGHAI);
        return today.isAfter(expireDate);
    }

    public static boolean isExpired(StoreMapper storeMapper, Long storeId) {
        return isExpired(resolveStore(storeMapper, storeId));
    }

    public static boolean isExpired(StoreMapper storeMapper, String storeId) {
        return isExpired(resolveStore(storeMapper, parseStoreId(storeId)));
    }

    public static boolean shouldHidePublicCatalog(Integer identityType, StoreMapper storeMapper, Long storeId) {
        if (identityType != null && identityType == 3) {
            return false;
        }
        return isExpired(storeMapper, storeId);
    }

    public static boolean shouldHidePublicCatalog(Integer identityType, StoreMapper storeMapper, String storeId) {
        if (identityType != null && identityType == 3) {
            return false;
        }
        return isExpired(storeMapper, storeId);
    }

    public static boolean isMerchantAccountExpired(User user, StoreMapper storeMapper) {
        if (user == null || user.getIdentityType() == null || user.getIdentityType() != 2) {
            return false;
        }
        return isExpired(findOwnStore(storeMapper, user.getId()));
    }

    public static Store findOwnStore(StoreMapper storeMapper, Long userId) {
        if (storeMapper == null || userId == null) {
            return null;
        }
        Store query = new Store();
        query.setUserId(userId);
        query.setDeleted(0);
        return firstStore(storeMapper.findStore(query));
    }

    public static Store resolveStore(StoreMapper storeMapper, Long storeId) {
        if (storeMapper == null || storeId == null) {
            return null;
        }
        Store byBusinessId = storeMapper.selectByStoreId(storeId);
        if (byBusinessId != null) {
            return byBusinessId;
        }
        Store query = new Store();
        query.setStoreId(storeId);
        query.setDeleted(0);
        Store hit = firstStore(storeMapper.findStore(query));
        if (hit != null) {
            return hit;
        }
        query = new Store();
        query.setId(storeId);
        query.setDeleted(0);
        return firstStore(storeMapper.findStore(query));
    }

    public static Store findByStoreId(StoreMapper storeMapper, Long storeId) {
        return resolveStore(storeMapper, storeId);
    }

    public static Long parseStoreId(String storeId) {
        if (!StringUtils.hasText(storeId)) {
            return null;
        }
        try {
            return Long.parseLong(storeId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String normalizeStoreTime(String storeTime) {
        LocalDate date = parseExpireDate(storeTime);
        return date == null ? null : date.format(DATE_FMT);
    }

    public static LocalDate parseExpireDate(String storeTime) {
        if (!StringUtils.hasText(storeTime)) {
            return null;
        }
        String raw = storeTime.trim();
        Matcher iso = ISO_DATE.matcher(raw);
        if (iso.find()) {
            try {
                return LocalDate.parse(iso.group(1), DATE_FMT);
            } catch (DateTimeParseException ignored) {
                // fall through
            }
        }
        Matcher slash = SLASH_DATE.matcher(raw);
        if (slash.find()) {
            try {
                return LocalDate.parse(slash.group(1) + "-" + slash.group(2) + "-" + slash.group(3), DATE_FMT);
            } catch (DateTimeParseException ignored) {
                // fall through
            }
        }
        LocalDate parsed = tryParse(raw);
        if (parsed != null) {
            return parsed;
        }
        if (raw.matches("\\d{13}")) {
            return Instant.ofEpochMilli(Long.parseLong(raw)).atZone(StoreOpenHelper.SHANGHAI).toLocalDate();
        }
        if (raw.matches("\\d{10}")) {
            return Instant.ofEpochSecond(Long.parseLong(raw)).atZone(StoreOpenHelper.SHANGHAI).toLocalDate();
        }
        return null;
    }

    private static LocalDate tryParse(String raw) {
        ZoneId shanghai = StoreOpenHelper.SHANGHAI;
        try {
            return LocalDateTime.parse(raw, DATETIME_FMT).atZone(shanghai).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(raw, DATETIME_MS_FMT).atZone(shanghai).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(raw.replace(' ', 'T'), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(shanghai)
                    .toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Instant.parse(raw).atZone(shanghai).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(raw).atZoneSameInstant(shanghai).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return ZonedDateTime.parse(raw).withZoneSameInstant(shanghai).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .withZoneSameInstant(shanghai)
                    .toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return ZonedDateTime.parse(raw, CTIME_FMT).withZoneSameInstant(shanghai).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private static Store firstStore(List list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Object first = list.get(0);
        return first instanceof Store ? (Store) first : null;
    }
}
