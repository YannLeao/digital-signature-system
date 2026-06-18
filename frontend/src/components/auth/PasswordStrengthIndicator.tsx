import {CheckCircle2, Circle} from 'lucide-react'
import {passwordRules} from '../../schemas/authSchemas'

type PasswordStrengthIndicatorProps = {
    password: string
}

export function PasswordStrengthIndicator({
                                              password,
                                          }: PasswordStrengthIndicatorProps) {
    const rules = Object.values(passwordRules)
    const metRules = rules.filter((rule) => rule.test(password)).length

    return (
        <div className="rounded-lg border border-[#374151] bg-[#111827] p-3.5 space-y-3">
            <div className="flex gap-1.5">
                {rules.map((rule, index) => (
                    <span
                        aria-label={rule.label}
                        className={`h-1.5 flex-1 rounded-full transition-colors duration-300 ${
                            index < metRules ? 'bg-[#06B6D4]' : 'bg-[#273142]'
                        }`}
                        key={rule.label}
                    />
                ))}
            </div>

            <ul className="grid gap-2 text-xs sm:grid-cols-2">
                {rules.map((rule) => {
                    const isMet = rule.test(password)

                    return (
                        <li
                            className={`flex items-center gap-2 transition-colors ${
                                isMet ? 'text-[#22D3EE] font-medium' : 'text-[#6B7280]'
                            }`}
                            key={rule.label}
                        >
                            {isMet ? (
                                <CheckCircle2 className="h-3.5 w-3.5 text-[#06B6D4] shrink-0" />
                            ) : (
                                <Circle className="h-3.5 w-3.5 text-[#374151] shrink-0" />
                            )}
                            <span>{rule.label}</span>
                        </li>
                    )
                })}
            </ul>
        </div>
    )
}