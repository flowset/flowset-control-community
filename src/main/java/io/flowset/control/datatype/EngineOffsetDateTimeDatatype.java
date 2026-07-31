package io.flowset.control.datatype;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.engine.EngineTimeService;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.TimeZoneAwareDatatype;
import io.jmix.core.metamodel.datatype.impl.AbstractTemporalDatatype;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQuery;
import java.util.Locale;
import java.util.TimeZone;

@DatatypeDef(id = "engineOffsetDateTime", javaClass = OffsetDateTime.class, value = "control_EngineOffsetDateTimeDatatype")
public class EngineOffsetDateTimeDatatype extends AbstractTemporalDatatype<OffsetDateTime> implements TimeZoneAwareDatatype {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected ApplicationContext applicationContext;

    public EngineOffsetDateTimeDatatype() {
        super(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public String format(@Nullable Object value, Locale locale, @Nullable TimeZone timeZone) {
        if (timeZone == null || value == null) {
            return format(value, locale);
        }
        return formatCurrentEngineOffsetDateTime((OffsetDateTime) value, locale, timeZone);
    }


    public String formatOffsetDateTime(@Nullable OffsetDateTime date, Locale locale, TimeZone timeZone) {
        if (date == null) {
            return "";
        }

        LocalDateTime timestamp = date.atZoneSameInstant(timeZone.toZoneId()).toLocalDateTime();
        return format(timestamp, locale);
    }

    public String formatCurrentEngineOffsetDateTime(@Nullable OffsetDateTime date, Locale locale, TimeZone timeZone) {
        if (date == null) {
            return "";
        }

        final EngineService engineService = applicationContext.getBean(EngineService.class);
        final BpmEngine selectedEngine = engineService.getSelectedEngine();
        if (selectedEngine == null) {
            return formatOffsetDateTime(date, locale, timeZone);
        }

        final EngineTimeService engineTimeService = applicationContext.getBean(EngineTimeService.class);
        final Long engineOffset = engineTimeService.getEngineOffset(selectedEngine.getId());
        if (engineOffset == null) {
            return formatOffsetDateTime(date, locale, timeZone);
        }
        final OffsetDateTime synchronizedDateTime = date.minus(engineOffset, ChronoUnit.MILLIS);
        return formatOffsetDateTime(synchronizedDateTime, locale, timeZone);
    }

    @Override
    protected DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    }

    @Override
    protected DateTimeFormatter getDateTimeFormatter(FormatStrings formatStrings, Locale locale) {
        return DateTimeFormatter.ofPattern(formatStrings.getDateTimeFormat(), locale);
    }

    @Override
    protected TemporalQuery<OffsetDateTime> newInstance() {
        return OffsetDateTime::from;
    }
}
