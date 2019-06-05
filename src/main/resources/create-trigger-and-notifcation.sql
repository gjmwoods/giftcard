--Note: Execute on both sources ephemeral and permanent

-- Trigger: notify_of_new_events

-- DROP TRIGGER notify_of_new_events ON public.domain_event_entry;

CREATE TRIGGER notify_of_new_events
    AFTER INSERT
    ON public.domain_event_entry
    FOR EACH ROW
    EXECUTE PROCEDURE public.notify_channel();

-- FUNCTION: public.notify_channel()
-- DROP FUNCTION public.notify_channel();

CREATE FUNCTION public.notify_channel()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
  BEGIN
    NOTIFY newEventsChannel;
    RETURN null;
  END;
$BODY$;

ALTER FUNCTION public.notify_channel() OWNER TO postgres;