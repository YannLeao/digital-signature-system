import { passwordRules } from '../../schemas/authSchemas'

type PasswordStrengthIndicatorProps = {
  password: string
}

export function PasswordStrengthIndicator({
  password,
}: PasswordStrengthIndicatorProps) {
  const rules = Object.values(passwordRules)
  const metRules = rules.filter((rule) => rule.test(password)).length

  return (
    <div className="rounded-lg border border-[#374151] bg-[#111827] p-3">
      <div className="mb-3 flex gap-1">
        {rules.map((rule, index) => (
          <span
            aria-label={rule.label}
            className={`h-1.5 flex-1 rounded-full ${
              index < metRules ? 'bg-[#06B6D4]' : 'bg-[#374151]'
            }`}
            key={rule.label}
          />
        ))}
      </div>
      <ul className="grid gap-2 text-xs text-[#9CA3AF] sm:grid-cols-2">
        {rules.map((rule) => {
          const isMet = rule.test(password)

          return (
            <li
              className={isMet ? 'text-[#06B6D4]' : 'text-[#9CA3AF]'}
              key={rule.label}
            >
              {isMet ? 'Atendido' : 'Pendente'}: {rule.label}
            </li>
          )
        })}
      </ul>
    </div>
  )
}
