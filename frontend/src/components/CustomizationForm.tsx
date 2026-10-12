import { useState, type FormEvent } from 'react';
import { ApiError } from '../api/client';
import { REGIONS, type Customization, type Region } from '../api/types';

const TAG_PATTERN = /^[a-z0-9][a-z0-9-]{0,19}$/;

interface Props {
  initial: Customization;
  submitting: boolean;
  error: unknown;
  onSubmit: (value: Customization) => void;
  onCancel: () => void;
}

/** Client-side checks mirror the backend rules for fast feedback; the server stays the source of truth. */
function validate(value: Customization, tags: string[]): Record<string, string> {
  const errors: Record<string, string> = {};
  if ((value.localizedName ?? '').length > 60) errors.localizedName = 'Max 60 characters';
  if (tags.length > 10) errors.tags = 'Max 10 tags';
  const bad = tags.find((t) => !TAG_PATTERN.test(t));
  if (bad) errors.tags = `Invalid tag "${bad}": lowercase letters, digits and dashes (max 20)`;
  if ((value.notes ?? '').length > 500) errors.notes = 'Max 500 characters';
  return errors;
}

export function CustomizationForm({ initial, submitting, error, onSubmit, onCancel }: Props) {
  const [localizedName, setLocalizedName] = useState(initial.localizedName ?? '');
  const [region, setRegion] = useState<Region | ''>(initial.region ?? '');
  const [tagsText, setTagsText] = useState(initial.tags.join(', '));
  const [notes, setNotes] = useState(initial.notes ?? '');
  const [clientErrors, setClientErrors] = useState<Record<string, string>>({});

  const serverErrors = error instanceof ApiError ? error.fieldErrors : {};
  const errors = { ...serverErrors, ...clientErrors };
  const tags = [...new Set(tagsText.split(',').map((t) => t.trim().toLowerCase()).filter(Boolean))];

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    const value: Customization = {
      localizedName: localizedName.trim() || null,
      region: region || null,
      tags,
      notes: notes.trim() || null,
    };
    const found = validate(value, tags);
    setClientErrors(found);
    if (Object.keys(found).length === 0) onSubmit(value);
  };

  return (
    <form className="form" onSubmit={handleSubmit} noValidate>
      <label>
        Localized name
        <input
          value={localizedName}
          onChange={(e) => setLocalizedName(e.target.value)}
          maxLength={60}
          placeholder="e.g. Pikachu (JP)"
          aria-invalid={!!errors.localizedName}
        />
        {errors.localizedName && <span className="field-error">{errors.localizedName}</span>}
      </label>

      <label>
        Region
        <select value={region} onChange={(e) => setRegion(e.target.value as Region | '')} aria-invalid={!!errors.region}>
          <option value="">— None —</option>
          {REGIONS.map((r) => (
            <option key={r} value={r}>
              {r.charAt(0) + r.slice(1).toLowerCase()}
            </option>
          ))}
        </select>
        {errors.region && <span className="field-error">{errors.region}</span>}
      </label>

      <label>
        Tags <small className="muted">(comma separated)</small>
        <input
          value={tagsText}
          onChange={(e) => setTagsText(e.target.value)}
          placeholder="starter, fire-team"
          aria-invalid={!!errors.tags}
        />
        {tags.length > 0 && (
          <div className="chips">
            {tags.map((t) => (
              <span key={t} className="chip">
                {t}
              </span>
            ))}
          </div>
        )}
        {errors.tags && <span className="field-error">{errors.tags}</span>}
      </label>

      <label>
        Notes
        <textarea value={notes} onChange={(e) => setNotes(e.target.value)} rows={4} maxLength={500} />
        <small className="muted counter">{notes.length}/500</small>
        {errors.notes && <span className="field-error">{errors.notes}</span>}
      </label>

      {error instanceof Error && Object.keys(serverErrors).length === 0 && (
        <p className="form-error" role="alert">
          {error.message}
        </p>
      )}

      <div className="form-actions">
        <button type="button" className="btn" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Saving…' : 'Save changes'}
        </button>
      </div>
    </form>
  );
}
